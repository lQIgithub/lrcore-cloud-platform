package com.lrcore.auth.controller;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSO 单点登录 —— AS 统一登录页（GET /login，服务端渲染）。
 * <p>
 * 渲染方式：加载 classpath:sso/login.html 模板，替换 CSRF 令牌与提示消息占位符
 * （无模板引擎依赖）。CSRF 令牌取自 CsrfFilter 写入的请求属性
 * （安全链启用 CSRF 后，CsrfToken 属性名 = CsrfToken.class.getName()）。
 * <p>
 * 页面为 AS 自包含：验证码同源 /sso/captcha 获取（Redis captcha_codes:{uuid}），
 * 登录 POST /login 由 LrcoreSsoLoginFilter 走平台安全策略认证；
 * 认证成功后回跳 SavedRequest（/oauth2/authorize），授权码流程自动续接。
 * <p>
 * 查询参数：
 * <ul>
 *   <li>{@code error}：登录失败原因（由 LrcoreSsoSecurityConfig 失败处理器携带）；</li>
 *   <li>{@code logout}：登出成功标识（/logout 成功回跳携带），展示“已退出”提示。</li>
 * </ul>
 * @ClassName SsoLoginController
 * @Author lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@Slf4j
@Hidden
@Controller
public class SsoLoginController {

    /**
     * 登录页 HTML 模板（占位符：__CSRF_TOKEN__ / __ERROR_MSG__ / __LOGOUT_FLAG__）。
     */
    private static final String TEMPLATE = loadTemplate();

    /**
     * 渲染 SSO 登录页。
     */
    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> loginPage(@RequestParam(name = "error", required = false) String error,
                                            @RequestParam(name = "logout", required = false) String logout,
                                            HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        String token = csrfToken != null ? csrfToken.getToken() : "";

        String html = TEMPLATE
                .replace("__CSRF_TOKEN__", escapeHtml(token))
                .replace("__ERROR_DISPLAY__", error != null && !error.isBlank() ? "block" : "none")
                .replace("__ERROR_MSG__", escapeHtml(error == null ? "" : error))
                .replace("__LOGOUT_FLAG__", logout != null ? "block" : "none");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    private static String loadTemplate() {
        try {
            return new String(
                    new ClassPathResource("sso/login.html").getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException("SSO 登录页模板加载失败: sso/login.html", ex);
        }
    }

    private static String escapeHtml(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
