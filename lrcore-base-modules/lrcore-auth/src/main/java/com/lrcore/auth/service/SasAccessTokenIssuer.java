package com.lrcore.auth.service;

import com.lrcore.common.auth.token.LrcoreTokenCustomizer;
import com.lrcore.common.auth.user.LrcoreUserDetails;
import com.lrcore.common.auth.sso.SsoStandardPrincipal;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.JwtGenerator;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSO/SAS 令牌签发服务 —— 供非浏览器交互场景（如第三方社交扫码回调）
 *            在平台用户经 SAS 认证链强认证后，直接走 SAS 同一套 JWT 签发机制授予
 *            标准 RS256 访问令牌（与 /oauth2/authorize 授权码流程签发的令牌
 *            同为 JWKS 公钥验签、claims 契约一致），彻底取代若依旧 HS512 双 Token
 *            签发（lrcore-common-security 的 TokenService/TokenDto）。
 * <p>
 * 实现要点：
 * <ul>
 *   <li>复用 SAS 公开 {@link JwtGenerator}：由平台 JWKSource 派生 {@link NimbusJwtEncoder}，
 *       挂载 {@link LrcoreTokenCustomizer} 注入业务 claims 契约；</li>
 *   <li>主体形式与 SSO 登录过滤链一致：{@code SsoStandardPrincipal.from(...)} 映射为
 *       SAS JDBC 可安全持久化的标准认证（Jackson 3 白名单类型）；</li>
 *   <li>签发后以 {@link OAuth2AuthorizationService} 持久化授权记录（principal = 用户名），
 *       使 SLO（back-channel logout / principal 撤销）可按用户名回收该令牌；</li>
 *   <li>访问令牌有效期取自登录所用客户端的 {@link TokenSettings}
 *       （默认 30 分钟），无 refresh_token（OAuth 2.1 / RFC 9700 方向，与公共客户端一致）。</li>
 * </ul>
 *
 * @ClassName: SasAccessTokenIssuer
 * @Author: lrcore
 * @Date 2026/8/24
 * @Version 1.0
 */
@Slf4j
@Service
public class SasAccessTokenIssuer {

    /**
     * 社交登录后签发令牌所依据的 OAuth2 客户端（与 SPA 主登录同客户端）。
     */
    public static final String SSO_CLIENT_ID = "web-admin-spa";

    private final OAuth2AuthorizationService authorizationService;
    private final RegisteredClientRepository registeredClientRepository;
    private final JwtGenerator jwtGenerator;
    private final AuthorizationServerSettings authorizationServerSettings;

    public SasAccessTokenIssuer(OAuth2AuthorizationService authorizationService,
                                RegisteredClientRepository registeredClientRepository,
                                JWKSource<SecurityContext> jwkSource,
                                AuthorizationServerSettings authorizationServerSettings,
                                LrcoreTokenCustomizer lrcoreTokenCustomizer) {
        this.authorizationService = authorizationService;
        this.registeredClientRepository = registeredClientRepository;
        this.authorizationServerSettings = authorizationServerSettings;
        JwtEncoder encoder = new NimbusJwtEncoder(jwkSource);
        JwtGenerator jwt = new JwtGenerator(encoder);
        jwt.setJwtCustomizer(lrcoreTokenCustomizer);
        this.jwtGenerator = jwt;
    }

    /**
     * 依据平台用户签发 SAS JWT 访问令牌。
     *
     * @param user   平台用户（已完成 SAS 强认证）
     * @param scopes 授权范围（缺省为 openid、profile）
     * @return SAS 访问令牌
     */
    public OAuth2AccessToken issueAccessToken(LrcoreUserDetails user, Set<String> scopes) {
        RegisteredClient client = registeredClientRepository.findByClientId(SSO_CLIENT_ID);
        if (client == null) {
            throw new IllegalStateException("SSO 客户端未注册: " + SSO_CLIENT_ID);
        }

        UsernamePasswordAuthenticationToken principal = SsoStandardPrincipal.from(user);

        // 预建授权记录（含已授权范围），JwtGenerator 依赖其读取 scope / claims
        Set<String> authorizedScopes = new LinkedHashSet<>(
                scopes == null || scopes.isEmpty() ? Set.of("openid", "profile") : scopes);
        OAuth2Authorization.Builder authBuilder =
                OAuth2Authorization.withRegisteredClient(client)
                        .principalName(principal.getName())
                        .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                        .authorizedScopes(authorizedScopes);
        OAuth2Authorization authorization = authBuilder.build();

        OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(client)
                .principal(principal)
                .authorizationServerContext(new DefaultAuthorizationServerContext(authorizationServerSettings))
                .authorization(authorization)
                .authorizedScopes(authorizedScopes)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrant(principal)
                .build();

        OAuth2Token token = jwtGenerator.generate(tokenContext);
        if (token == null) {
            throw new IllegalStateException("SAS 访问令牌签发失败");
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                token.getTokenValue(),
                token.getIssuedAt(),
                token.getExpiresAt(),
                authorizedScopes);

        // 持久化授权记录，使 SLO 可按 principal_name 撤销该令牌
        OAuth2Authorization saved = OAuth2Authorization.from(authorization)
                .token(accessToken,
                        (meta) -> meta.put(OAuth2Authorization.Token.INVALIDATED_METADATA_NAME, false))
                .build();
        authorizationService.save(saved);

        log.info("SAS 令牌签发成功, principal: {}, scope: {}, expires: {}",
                principal.getName(), authorizedScopes, accessToken.getExpiresAt());
        return accessToken;
    }

    /**
     * SAS 7.1 不再提供 {@code DefaultAuthorizationServerContext}，
     * 以授权服务器设置实现最小 {@link AuthorizationServerContext}。
     */
    private static final class DefaultAuthorizationServerContext implements AuthorizationServerContext {
        private final AuthorizationServerSettings settings;
        private DefaultAuthorizationServerContext(AuthorizationServerSettings settings) {
            this.settings = settings;
        }
        @Override
        public String getIssuer() {
            return settings.getIssuer();
        }
        @Override
        public AuthorizationServerSettings getAuthorizationServerSettings() {
            return settings;
        }
    }
}