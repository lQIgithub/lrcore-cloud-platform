package com.lrcore.gateway.config;

import com.lrcore.common.core.constant.HttpStatus;
import com.lrcore.common.core.utils.ServletUtils;
import com.lrcore.gateway.config.properties.IgnoreWhiteProperties;
import com.lrcore.gateway.config.properties.Oauth2ServerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 网关统一安全过滤链（Spring Security 7 WebFlux / 资源服务器适配）。
 * <p>
 * 背景：lrcore-common-gateway 引入 spring-boot-starter-oauth2-resource-server 后，Spring Security
 * 默认会把网关所有路由全部拦截（401/403），导致前端所有接口（含登录、验证码、业务接口）都无法访问。
 * 本配置显式提供一条与现有网关鉴权（AuthFilter 的 SSO JWT 校验）兼容的过滤链：
 * <ul>
 *   <li>放行公开地址（登录、登出、验证码、OAuth2 端点、OIDC 发现、Swagger、Actuator），
 *       并叠加 Nacos 配置的 security.ignore.whites 白名单；</li>
 *   <li>其余路由走 OAuth2 Resource Server 的 JWT 校验 —— 统一由授权服务器（SAS）
 *       RS256 JWT（JWKS 公钥 + iss 校验）校验，全面取代若依旧 HS512 双 Token 链路；
 *       未认证/令牌无效统一返回 401 JSON。</li>
 * </ul>
 * <p>
 * <b>关键修复（配置白名单仍被 401 的根因）：</b>
 * Spring Security 7.x 中 Bearer 令牌校验失败时，默认失败处理会<b>直接调用
 * AuthenticationEntryPoint 返回 401</b>——该动作发生在授权判定（permitAll/白名单）之前，
 * 因此只要请求头携带了无法通过解码的令牌（令牌过期、脏数据），
 * 即使路径已配置白名单也会被 401 拦截。此处显式设置
 * {@code authenticationFailureHandler}：校验失败时<b>按匿名上下文继续过滤链</b>
 * （注意：WebFlux 下失败处理器返回的 Mono 替代过滤器结果，必须显式
 * {@code webFilterExchange.getChain().filter(...)}，返回 {@code Mono.empty()} 会导致
 * 请求以空 200 提前结束），让 permitAll/白名单 的授权判定成为最终裁决：
 * <ul>
 *   <li>白名单/公开路径 + 无效令牌 → 放行（由下游自行处理）；</li>
 *   <li>受保护路径 + 无效令牌 → 授权判定失败 → 401 JSON。</li>
 * </ul>
 *
 * @ClassName: GatewaySecurityConfig
 * @Author: lrcore
 * @Date 2026/8/24
 * @Version: 2.0
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class GatewaySecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(GatewaySecurityConfig.class);
    private final IgnoreWhiteProperties ignoreWhite;
    private final Oauth2ServerProperties oauth2;

    /**
     * 网关公开放行地址（硬编码基线；Nacos security.ignore.whites 可在此基础上叠加）。
     */
    private static final String[] PERMIT_PATHS = {
            // ===== Swagger / Actuator / 静态资源 =====
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/v3/api-docs.yml",
            "/webjars/**",
            "/doc.html",
            "/actuator/**",
            "/favicon.ico",
            "/error",
            // ===== 网关自身验证码路由（RouterFunctionConfiguration） =====
            "/api/v1/auth/captcha",
            // ===== 认证中心（lrcore-auth）第三方社交登录入口（SSO 授权码流程） =====
            "/lrcore-auth/api/v1/auth/social/**",
            // ===== 授权服务器协议端点（经网关对外暴露，OIDC 授权码流程必需） =====
            "/lrcore-auth/oauth2/**",
            "/lrcore-auth/.well-known/**",
            "/lrcore-auth/userinfo",
            "/lrcore-auth/connect/logout",
            // ===== SSO 单点登录宿主侧（lrcore-auth 自持登录页/验证码/登录提交/登出） =====
            "/lrcore-auth/login",
            "/lrcore-auth/logout",
            "/lrcore-auth/sso/**"
    };

    /**
     * 网关资源服务器安全过滤链。
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public SecurityWebFilterChain gatewaySecurityWebFilterChain(ServerHttpSecurity http) {
        // @formatter:off
        http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                // 网关统一认证入口：未认证返回 401 JSON
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler((exchange, denied) ->
                                unauthorizedResponse(exchange, "没有权限访问该接口")))
                .authorizeExchange((authorize) -> {
                    // 硬编码公开地址 + Nacos 配置的 security.ignore.whites 白名单
                    authorize.pathMatchers(PERMIT_PATHS).permitAll();
                    List<String> whites = ignoreWhite != null ? ignoreWhite.getWhites() : List.of();
                    if (whites != null) {
                        for (String pattern : whites) {
                            if (pattern != null && !pattern.isBlank()) {
                                authorize.pathMatchers(pattern).permitAll();
                            }
                        }
                    }
                    authorize.anyExchange().authenticated();
                })
                // 资源服务器：JWT 校验（双轨解码器：旧 HS512 + SAS RS256）
                .oauth2ResourceServer((oauth2) -> {
                    // [关键] Bearer 令牌校验失败不再直接 401，而是按匿名继续过滤链，
                    // 交由授权判定（白名单/permitAll vs authenticated）裁决，
                    // 恢复“白名单路径放行”的语义（详见类注释“关键修复”）。
                    // 注意：WebFlux 的 AuthenticationWebFilter 中，失败处理器返回的 Mono 会
                    // <b>替代</b>过滤器自身的结果——若返回 Mono.empty() 会导致请求直接以空 200 结束、
                    // 后续过滤器（含授权判定）不执行。因此这里必须显式调用
                    // webFilterExchange.getChain().filter(...) 让链路按匿名上下文继续。
                    oauth2.authenticationFailureHandler((webFilterExchange, ex) -> {
                        log.debug("[网关鉴权] Bearer 令牌校验失败，按匿名继续过滤链: {}", ex.getMessage());
                        return webFilterExchange.getChain().filter(webFilterExchange.getExchange());
                    });
                    oauth2.jwt(Customizer.withDefaults())
                            .authenticationEntryPoint(authenticationEntryPoint());
                });
        // @formatter:on
        return http.build();
    }

    /**
     * 网关 JWT 解码器（@Primary，统一由授权服务器 SAS RS256 验签）：
     * 经 JWKS 公钥验签，并校验 iss/exp 等标准声明。若依遗留的 HS512 双 Token
     * 解码轨道已全面移除，网关仅接受 SAS 授权服务器签发的 RS256 JWT。
     * <p>
     * 配置见 {@link Oauth2ServerProperties}（lrcore.oauth2.*）；issuer 未配置时返回
     * <b>恒失败的解码器</b>（保证 Bean 恒存在），即网关拒绝非 SAS 令牌。
     *
     * @return 响应式 JWT 解码器
     */
    @Bean
    @Primary
    public ReactiveJwtDecoder lrcoreReactiveJwtDecoder() {
        return buildSasReactiveJwtDecoder();
    }

    /**
     * 授权服务器（SAS）RS256 JWT 解码器：经 JWKS 公钥验签，并校验 iss/exp 等标准声明。
     *
     * @return 响应式 JWT 解码器
     */
    @Bean
    public ReactiveJwtDecoder lrcoreSasReactiveJwtDecoder() {
        return buildSasReactiveJwtDecoder();
    }

    private ReactiveJwtDecoder buildSasReactiveJwtDecoder() {
        String issuer = oauth2.getIssuer();
        if (issuer == null || issuer.isBlank()) {
            log.info("[网关鉴权] 未配置 lrcore.oauth2.issuer，禁用授权服务器令牌校验");
            return token -> Mono.error(new JwtException("未启用授权服务器令牌校验（lrcore.oauth2.issuer 未配置）"));
        }
        String jwkSetUri = oauth2.getJwkSetUri() != null && !oauth2.getJwkSetUri().isBlank()
                ? oauth2.getJwkSetUri()
                : issuer + "/oauth2/jwks";
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithms(algorithms -> algorithms.add(SignatureAlgorithm.RS256))
                .build();
        decoder.setJwtValidator(oauth2.isValidateIssuer()
                ? JwtValidators.createDefaultWithIssuer(issuer)
                : JwtValidators.createDefault());
        return decoder;
    }

    private ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, authException) -> unauthorizedResponse(exchange, "登录状态已过期或令牌无效");
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        return ServletUtils.webFluxResponseWriter(
                exchange.getResponse(),
                message,
                HttpStatus.UNAUTHORIZED + "");
    }
}
