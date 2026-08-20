package com.lrcore.gateway.config;

import com.lrcore.common.core.constant.HttpStatus;
import com.lrcore.common.core.utils.JwtUtils;
import com.lrcore.common.core.utils.ServletUtils;
import com.lrcore.gateway.config.properties.IgnoreWhiteProperties;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
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
 *   <li>其余路由走 OAuth2 Resource Server 的 JWT 校验——使用与旧双 Token 链路一致的
 *       {@link JwtUtils}（HS512 + 共享密钥）解码，校验签名与时效；</li>
 *   <li>未认证/令牌无效统一返回 401 JSON（与 AuthFilter 返回体一致，供前端识别 TOKEN 过期）。</li>
 * </ul>
 *
 * @ClassName: GatewaySecurityConfig
 * @Author: lrcore
 * @Date: 2026/8/21
 * @Version: 1.0
 */
@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewaySecurityConfig.class);

    /** 网关公开放行地址。 */
    private static final String[] PERMIT_PATHS = {
            "/auth/login", "/auth/logout", "/auth/refresh",
            "/auth/captcha", "/auth/captchaImage",
            "/lrcore-auth/**", "/oauth2/**", "/.well-known/**",
            "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**",
            "/v3/api-docs/**", "/v3/api-docs.yaml", "/v3/api-docs.yml",
            "/webjars/**", "/doc.html",
            "/actuator/**", "/favicon.ico", "/error"
    };

    private final IgnoreWhiteProperties ignoreWhite;

    public GatewaySecurityConfig(IgnoreWhiteProperties ignoreWhite) {
        this.ignoreWhite = ignoreWhite;
    }

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
                    authorize.pathMatchers(PERMIT_PATHS).permitAll();
                    // 叠加 Nacos 配置的 security.ignore.whites 白名单
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
                // 资源服务器：JWT 校验（自定义解码器兼容旧 HS512 双 Token）
                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(authenticationEntryPoint()));
        // @formatter:on
        return http.build();
    }

    /**
     * 网关 JWT 解码器：与旧链路 {@link JwtUtils} 一致（HS512 + 共享密钥），校验签名与时效。
     *
     * @return 响应式 JWT 解码器
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return token -> {
            try {
                return Mono.just(buildJwt(token));
            } catch (JwtException e) {
                throw e;
            } catch (Exception e) {
                log.debug("网关 JWT 解码失败: {}", e.getMessage());
                throw new JwtException("令牌无效或已过期", e);
            }
        };
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

    private Jwt buildJwt(String token) {
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