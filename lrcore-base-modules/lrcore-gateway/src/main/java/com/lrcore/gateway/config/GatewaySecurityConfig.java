package com.lrcore.gateway.config;

import com.lrcore.common.core.constant.HttpStatus;
import com.lrcore.common.core.utils.JwtUtils;
import com.lrcore.common.core.utils.ServletUtils;
import com.lrcore.gateway.config.properties.IgnoreWhiteProperties;
import com.lrcore.gateway.config.properties.Oauth2ServerProperties;
import io.jsonwebtoken.Claims;
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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 网关统一安全过滤链（Spring Security 7 WebFlux / 资源服务器适配）。
 * <p>
 * 背景：lrcore-common-gateway 引入 spring-boot-starter-oauth2-resource-server 后，Spring Security
 * 默认会把网关所有路由全部拦截（401/403），导致前端所有接口（含登录、验证码、业务接口）都无法访问。
 * 本配置显式提供一条与现有网关鉴权（AuthFilter 的 JWT + Redis 校验）兼容的过滤链：
 * <ul>
 *   <li>放行公开地址（登录、登出、刷新、验证码、OAuth2 端点、OIDC 发现、Swagger、Actuator），
 *       并叠加 Nacos 配置的 security.ignore.whites 白名单；</li>
 *   <li>其余路由走 OAuth2 Resource Server 的 JWT 校验——双轨解码器：
 *       旧 HS512 双令牌（共享密钥，{@link JwtUtils}）优先，失败后回退授权服务器
 *       RS256 JWT（JWKS 公钥 + iss 校验），兼容旧前端与新 OIDC 登录双轨并行；</li>
 *   <li>未认证/令牌无效统一返回 401 JSON（与 AuthFilter 返回体一致，供前端识别 TOKEN 过期）。</li>
 * </ul>
 * <p>
 * <b>关键修复（配置白名单仍被 401 的根因）：</b>
 * Spring Security 7.x 中 Bearer 令牌校验失败时，默认失败处理会<b>直接调用
 * AuthenticationEntryPoint 返回 401</b>——该动作发生在授权判定（permitAll/白名单）之前，
 * 因此只要请求头携带了无法通过解码的令牌（旧令牌过期、SAS 新令牌、脏数据），
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
 * @Date: 2026/8/21
 * @Version: 1.1
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
            // ===== 认证中心（lrcore-auth）存量公开端点：登录/刷新/登出/注册/解锁等 =====
            "/lrcore-auth/api/v1/auth/**",
            // ===== 授权服务器协议端点（经网关对外暴露，OIDC 授权码流程必需） =====
            "/lrcore-auth/oauth2/**",
            "/lrcore-auth/.well-known/**",
            "/lrcore-auth/userinfo",
            "/lrcore-auth/connect/logout"
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
     * 网关 JWT 解码器（双轨，供资源服务器过滤链使用，@Primary）：
     * <ol>
     *   <li>旧双 Token 链路：HS512 + 共享密钥（{@link JwtUtils}），校验签名与时效；</li>
     *   <li>旧格式校验失败后回退：授权服务器 RS256 JWT，JWKS 公钥验签 + iss/exp 校验
     *       （见 {@link #lrcoreSasReactiveJwtDecoder()}）。</li>
     * </ol>
     * 两轨都失败时抛出 {@link JwtException}，由失败处理器按匿名放行（受保护路径再由授权判定 401）。
     *
     * @return 响应式 JWT 解码器
     */
    @Bean
    @Primary
    public ReactiveJwtDecoder lrcoreReactiveJwtDecoder() {
        ReactiveJwtDecoder legacyDecoder = token -> {
            try {
                return Mono.just(buildLegacyJwt(token));
            } catch (JwtException e) {
                throw e;
            } catch (Exception e) {
                // 旧格式令牌解析失败（签名不符/过期/格式错误）——交由调用方决定是否回退
                throw new JwtException("旧格式令牌校验失败: " + e.getMessage(), e);
            }
        };
        ReactiveJwtDecoder sasDecoder = buildSasReactiveJwtDecoder();
        return token -> legacyDecoder.decode(token)
                .onErrorResume(ex -> {
                    if (ex instanceof JwtException) {
                        log.debug("[网关鉴权] 旧 HS512 令牌校验失败，回退授权服务器令牌校验: {}", ex.getMessage());
                        return sasDecoder.decode(token);
                    }
                    return Mono.error(ex);
                });
    }

    /**
     * 授权服务器（SAS）RS256 JWT 解码器：经 JWKS 公钥验签，并校验 iss/exp 等标准声明。
     * <p>
     * 配置见 {@link Oauth2ServerProperties}（lrcore.oauth2.*）；issuer 未配置时返回
     * <b>恒失败的解码器</b>（保证 Bean 恒存在，不影响双轨解码器组合），
     * 即仅兼容旧 HS512 链路。
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
            log.info("[网关鉴权] 未配置 lrcore.oauth2.issuer，禁用授权服务器令牌校验（仅兼容旧 HS512 双令牌）");
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

    /**
     * 将旧链路 Claims 包装为 Spring 资源服务器的 {@link Jwt}。
     */
    private Jwt buildLegacyJwt(String token) {
        Claims claims = JwtUtils.parseToken(token);
        Map<String, Object> headers = new HashMap<>(2);
        headers.put("alg", "HS512");
        headers.put("typ", "JWT");
        Instant issuedAt = claims.getIssuedAt() != null
                ? claims.getIssuedAt().toInstant()
                : Instant.now();
        Instant expiresAt = claims.getExpiration() != null
                ? claims.getExpiration().toInstant()
                : null;
        return new Jwt(token, issuedAt, expiresAt, headers, claims);
    }
}
