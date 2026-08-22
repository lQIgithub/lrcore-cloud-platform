package com.lrcore.auth.config;

import com.lrcore.common.auth.api.LrcoreUsernamePasswordToken;
import com.lrcore.common.auth.sso.SsoStandardPrincipal;
import com.lrcore.common.auth.user.LrcoreUserDetails;
import com.lrcore.common.core.utils.ip.IpUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;

import java.io.IOException;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSO 单点登录 —— 平台登录页认证过滤器（POST /login）。
 *            脚手架 formLogin 自带的 UsernamePasswordAuthenticationFilter 只会构造
 *            UsernamePasswordAuthenticationToken，无法承载验证码/来源 IP 等安全上下文，
 *            也就无法走平台的 {@code PasswordLrcoreAuthenticator} 策略链
 *            （IP黑名单 → IP防暴破 → 验证码 → 账户锁定 → BCrypt）。
 * <p>
 * 本过滤器构造 {@link LrcoreUsernamePasswordToken}（携带 captcha / captchaId / ip），
 * 提交给容器 AuthenticationManager（含 LrcoreSsoHostConfiguration 装配的
 * PasswordLrcoreAuthenticator）。认证成功后由基类默认逻辑回跳 SavedRequest
 * （即被登录页中断的 /oauth2/authorize 请求），授权码流程自动续接。
 * <p>
 * 失败处理由 {@link LrcoreSsoSecurityConfig} 注入
 * （重定向 /login?error=...，登录页展示具体原因：验证码错误/密码错误/锁定等）。
 * @ClassName LrcoreSsoLoginFilter
 * @Author lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@Slf4j
public class LrcoreSsoLoginFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * 登录页表单参数字段名（与 classpath:sso/login.html 的表单控件一致）。
     */
    public static final String USERNAME_PARAMETER = "username";
    public static final String PASSWORD_PARAMETER = "password";
    public static final String CAPTCHA_PARAMETER = "captcha";
    public static final String CAPTCHA_ID_PARAMETER = "captchaId";

    public LrcoreSsoLoginFilter() {
        super("/login");
        // 仅 POST /login 触发认证（GET /login 为登录页，由控制器渲染）
        setRequiresAuthenticationRequestMatcher(new AndRequestMatcher(
                request -> "POST".equals(request.getMethod()),
                request -> "/login".equals(request.getRequestURI())));
    }

    /**
     * 从表单参数构造统一密码认证令牌并提交认证。
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        String username = clean(request.getParameter(USERNAME_PARAMETER));
        String password = clean(request.getParameter(PASSWORD_PARAMETER));
        String captcha = clean(request.getParameter(CAPTCHA_PARAMETER));
        String captchaId = clean(request.getParameter(CAPTCHA_ID_PARAMETER));

        if (username == null || password == null) {
            throw new BadCredentialsException("用户名或密码不能为空");
        }

        LrcoreUsernamePasswordToken authenticationToken = LrcoreUsernamePasswordToken
                .unauthenticated(username, password)
                .captcha(captcha)
                .captchaId(captchaId)
                .ip(IpUtils.getIpAddr(request))
                .clientType("sso-web");

        AuthenticationManager authenticationManager = getAuthenticationManager();
        if (authenticationManager == null) {
            throw new BadCredentialsException("认证服务不可用，请稍后重试");
        }
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        // [关键，SAS 7.1 / Jackson 3] 把平台认证结果替换为标准主体再写入 SecurityContext：
        // SAS 的 JdbcOAuth2AuthorizationService 以 @class 默认类型信息持久化 principal，
        // Jackson 3.1.4 内置类型校验器拒绝 com.lrcore.* 自定义类（且自定义
        // PolymorphicTypeValidator 在该路径不生效），若直接持久化 LrcoreUsernamePasswordToken，
        // 令牌端点回读授权时抛 IllegalArgumentException（/oauth2/token 500）。
        // 替换为 User + claim: 编码 authorities 后，全部为白名单内标准类型，
        // 业务 claims 由 LrcoreTokenCustomizer 在签发令牌时解码。
        if (authResult.getPrincipal() instanceof LrcoreUserDetails lrcoreUser) {
            authResult = SsoStandardPrincipal.from(lrcoreUser);
        }
        log.info("SSO 登录认证成功: {}", authResult.getName());
        super.successfulAuthentication(request, response, chain, authResult);
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
