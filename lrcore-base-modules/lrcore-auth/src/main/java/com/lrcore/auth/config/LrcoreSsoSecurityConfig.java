package com.lrcore.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSO 单点登录 —— lrcore-auth 的宿主安全过滤链（@Order(2)）。
 *            本 Bean 的存在使 starter 脚手架的 defaultSecurityFilterChain
 *            （@ConditionalOnMissingBean(SecurityFilterChain)）自动让位；
 *            授权服务器协议链（starter @Order(1)，仅匹配 /oauth2/* 等协议端点）
 *            不受影响，照常处理授权码/令牌/JWKS/登出等协议请求。
 * <p>
 * 职责：
 * <ul>
 *   <li>放行 SSO 公开端点（GET /login 登录页、/sso/** 验证码）与第三方社交登录入口
 *       （/api/v1/auth/social/**）；</li>
 *   <li>{@link LrcoreSsoLoginFilter}（POST /login）：平台安全策略认证
 *       （验证码 + IP黑名单 + 防暴破 + 锁定 + BCrypt，见 PasswordLrcoreAuthenticator），
 *       成功后回跳 SavedRequest（/oauth2/authorize），失败重定向 /login?error=...；</li>
 *   <li>会话：空闲 30 分钟 / 绝对 2 小时 —— 该会话即 SSO 单点登录的载体
 *       （会话有效期内所有 RP(依赖方) 免登）；</li>
 *   <li>登出（GET/POST /logout）：销毁 AS 会话 → 触发 LogoutSuccessEvent →
 *       starter 的 LrcoreBackChannelLogoutPublisher 撤销该用户全部授权
 *       并向已注册 RP 推送 back-channel logout_token（SSO 单点登出），
 *       随后回跳 /login?logout=1。</li>
 * </ul>
 * CSRF：仅 POST /login 强制（登录页携带 _csrf 隐藏域），
 * 存量无会话 JSON 接口不受影响。
 * @ClassName LrcoreSsoSecurityConfig
 * @Author lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
public class LrcoreSsoSecurityConfig {

    /**
     * 子门户页路径（SSO 登录成功后的默认落地页，展示多个子系统清单）。
     */
    public static final String PORTAL_PATH = "/sso/portal.html";

    /**
     * AS 服务内匿名可访问路径（存量公开端点 + SSO 登录端点）。
     */
    private static final String[] PUBLIC_PATHS = {
            // SSO 登录（登录页 + 验证码）
            "/login",
            "/sso/**",
            // 登出（浏览器 GET 回跳场景 + 表单 POST 兼容）
            "/logout",
            // 第三方社交登录（SSO 授权码流程外部入口，匿名可访问）
            "/api/v1/auth/social/**",
            // 数据库连接加解密工具（DevOps 直连调用，匿名放行；清单见
            // 技术文档/nacos配置/前后端忽视加解密接口白名单.md）
            "/api/v1/jasypt/**",
            // 许可证校验
            "/api/license/**",
            // OAuth2 客户端管理（平台内部专用：宿主安全链放行，
            // 真正鉴权由 @InnerAuth 校验 from-source: inner 请求头承担）
            "/api/v1/client/admin/**",
            "/error",
            "/favicon.ico",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/v3/api-docs.yml",
            "/webjars/**",
            "/doc.html",
            "/actuator/**"
    };

    /**
     * 宿主默认安全过滤链：SSO 登录页 + 存量公开端点 + 会话 + 登出。
     * <p>
     * Bean 名必须为 {@code defaultSecurityFilterChain}：
     * lrcore-common-security 的 LrcoreResourceServerSecurityConfiguration 以
     * resourceServerFilterChain / defaultSecurityFilterChain / authorizationServerSecurityFilterChain
     * 三个 Bean 名为让位条件，本链存在时其业务资源服务器链自动不装配；
     * 同时满足 starter 脚手架默认链的 @ConditionalOnMissingBean(SecurityFilterChain) 让位。
     */
    @Bean("defaultSecurityFilterChain")
    @Order(2)
    public SecurityFilterChain lrcoreSsoSecurityFilterChain(HttpSecurity http,
                                                            ObjectProvider<AuthenticationProvider> providers)
            throws Exception {
        // 由 AuthenticationProvider Bean 直接构建 ProviderManager（LrcoreSsoHostConfiguration
        // 的 PasswordLrcoreAuthenticator 策略链）。
        // 不注入容器全局 AuthenticationManager Bean：该 Bean 与 WebSecurityConfiguration 的
        // 过滤链装配存在创建期循环依赖（链 Bean 构造期需要管理器，管理器又依赖安全装配上下文）。
        AuthenticationManager authenticationManager =
                new ProviderManager(providers.orderedStream().toList());
        LrcoreSsoLoginFilter loginFilter = new LrcoreSsoLoginFilter();
        loginFilter.setAuthenticationManager(authenticationManager);
        // 登录失败：重定向登录页并携带具体原因（验证码错误/密码错误/账户锁定等）
        loginFilter.setAuthenticationFailureHandler(this::sendErrorToLoginPage);
        // [子门户需求] SSO 登录成功后一律进入"子系统展示门户"：不直接进入某一系统主页，
        // 而是先展示可进入的子系统清单，点击后再进入对应管理后台。
        // （从某子系统直达登录时，SavedRequest(/oauth2/authorize) 不再强行续接，
        //   登录后仍回到门户统一选择，保证"登录成功→门户"这一核心体验一致。）
        loginFilter.setAuthenticationSuccessHandler((request, response, authentication) ->
                response.sendRedirect(request.getContextPath() + PORTAL_PATH));

        // @formatter:off
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated())
                // 平台登录过滤器（替代脚手架 formLogin 的默认令牌过滤器）
                .addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class)
                // 仅登录 POST 强制 CSRF（登录页嵌入 _csrf 隐藏域）
                .csrf((csrf) -> csrf.requireCsrfProtectionMatcher(
                        request -> "POST".equals(request.getMethod())
                                && "/login".equals(request.getRequestURI())))
                // AS 会话 = SSO 单点登录载体。
                // 会话空闲超时由 server.servlet.session.timeout 控制（Nacos 下发，建议 30m；
                // Security 7.1 的 SessionManagementConfigurer 已移除 maximum*Time DSL 方法）
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                // [关键，Security 7.1 行为变更] 7.1 默认 requireExplicitSave=true：
                // 链上只有 SecurityContextHolderFilter（仅加载不保存），登录成功后
                // 安全上下文不会写入会话 → SSO 免登失效。
                // 显式 requireExplicitSave(false) → 装配 SecurityContextPersistenceFilter
                // （加载 + 认证后保存/登出后清理）。
                .securityContext((securityContext) -> securityContext
                        .requireExplicitSave(false))
                // 登出：销毁会话 → LogoutSuccessEvent → BCL 单点登出联动 → 回跳登录页。
                // [关键] 默认 logoutUrl 只注册 POST 匹配器；SSO 单点登出走浏览器
                // 顶层 GET 重跳（SPA 直接 location 到 /connect/logout → 本服务 /logout），
                // 必须显式 GET+POST 双匹配，否则 GET /logout 404。
                .logout((logout) -> logout
                        .logoutRequestMatcher(new OrRequestMatcher(
                                PathPatternRequestMatcher.pathPattern(HttpMethod.GET, "/logout"),
                                PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/logout")))
                        .logoutSuccessUrl("/login?logout=1"));
        // @formatter:on
        return http.build();
    }

    /**
     * 登录失败统一重定向：/login?error=原因（登录页读取并展示）。
     */
    private void sendErrorToLoginPage(HttpServletRequest request,
                                      HttpServletResponse response,
                                      AuthenticationException exception) throws IOException {
        String message = exception.getMessage() == null || exception.getMessage().isBlank()
                ? "登录失败"
                : exception.getMessage();
        String target = "/login?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
        if (isBrowserRequest(request)) {
            response.sendRedirect(target);
        } else {
            // 非浏览器客户端（如接口直连）返回 401 JSON，避免循环重定向
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"code\":401,\"msg\":\"" + message.replace("\"", "'") + "\"}");
        }
    }

    private static boolean isBrowserRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);
    }
}
