package com.lrcore.auth.controller;

import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.api.RemoteSysAppApi;
import com.lrcore.system.api.dto.PortalAppDto;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSO 单点登录 —— 子门户展示页（多个子系统的导航首页）。
 *            <ul>
 *              <li>{@code GET /sso/portal.html}：服务端渲染静态门户页（classpath:sso/portal.html）；</li>
 *              <li>{@code GET /sso/portal/apps}：返回可进入的子系统清单（经 lrcore-system-api 的
 *                  {@link RemoteSysAppApi} 远程读取 sys_app 启用数据）。</li>
 *            </ul>
 *            页面为 AS 自包含：加载后以相对路径调 {@code /sso/portal/apps} 拉取子系统，
 *            渲染为卡片；点击某张卡片即进入该系统后台（经各子系统自身的 SSO 授权码流程，
 *            SAS 会话已认证 → 免登直接落地对应管理后台）。
 *            <p>
 *            安全说明：{@code /sso/**} 在安全链中为公开端点（无需登录即可展示门户），
 *            但进入任一子系统后仍有网关 SSO 鉴权兜底，未认证跳转会被拦截。
 * @ClassName: SsoPortalController
 * @Author: lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@Slf4j
@Hidden
@Controller
public class SsoPortalController {

    /**
     * 门户页 HTML 模板。
     */
    private static final String PORTAL_TEMPLATE = loadTemplate();

    private final RemoteSysAppApi remoteSysAppApi;
    /**
     * 子系统未配置 app_url 时，门户默认进入的管理后台地址。
     * 通过配置 lrcore.sso.portal.default-admin-url 覆盖；缺省指向当前管理后台 SPA
     * （开发环境默认 http://localhost:3000，生产按网关/前端实际地址配置）。
     */
    private final String defaultAdminUrl;

    public SsoPortalController(RemoteSysAppApi remoteSysAppApi,
                               @Value("${lrcore.sso.portal.default-admin-url:http://localhost:3000}") String defaultAdminUrl) {
        this.remoteSysAppApi = remoteSysAppApi;
        this.defaultAdminUrl = defaultAdminUrl;
    }

    /**
     * 门户页（无需登录即可访问的门户聚合入口）。
     */
    @GetMapping(value = "/sso/portal.html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> portalPage() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(PORTAL_TEMPLATE);
    }

    /**
     * 门户子系统清单 JSON。
     */
    @GetMapping(value = "/sso/portal/apps", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResult<List<PortalAppDto>>> portalApps() {
        ApiResult<List<PortalAppDto>> result = remoteSysAppApi.portalList();
        if (result == null || !result.isSuccess() || result.getData() == null) {
            log.warn("门户子系统清单加载失败: {}", result == null ? "返回为空" : result.getMessage());
            return ResponseEntity.ok(ApiResult.fail("门户子系统清单加载失败"));
        }
        // 子系统未配置 app_url 时，回退到默认管理后台地址，保证门户卡片可点击进入
        result.getData().forEach(app -> {
            if (app.getAppUrl() == null || app.getAppUrl().isBlank()) {
                app.setAppUrl(defaultAdminUrl);
            }
        });
        return ResponseEntity.ok(ApiResult.success(result.getData()));
    }

    private static String loadTemplate() {
        try {
            return new String(
                    new ClassPathResource("sso/portal.html").getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new UncheckedIOException("SSO 门户页模板加载失败: sso/portal.html", ex);
        }
    }
}