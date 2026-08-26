package com.lrcore.auth.controller;

import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.api.RemoteSysAppApi;
import com.lrcore.system.api.dto.PortalAppDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SsoPortalController 单测：门户子系统清单的 app_url 兜底解析。
 */
class SsoPortalControllerTest {

    private static final String DEFAULT_ADMIN = "http://localhost:3000";

    @Test
    void blankAppUrl_resolvedToDefaultAdminUrl() {
        PortalAppDto app = new PortalAppDto();
        app.setAppName("系统管理平台");
        app.setAppCode("system");
        app.setAppUrl(null); // sys_app 中未配置 app_url 的场景

        Integer[] calls = {0};
        RemoteSysAppApi api = () -> {
            calls[0]++;
            return ApiResult.success(List.of(app));
        };

        SsoPortalController controller = new SsoPortalController(api, DEFAULT_ADMIN);
        var body = controller.portalApps().getBody();
        assertThat(body).isNotNull();
        assertThat(body.isSuccess()).isTrue();
        assertThat(calls[0]).isEqualTo(1);
        assertThat(body.getData()).hasSize(1);
        // 空 app_url 应被兜底为默认管理后台地址，保证门户卡片可点击进入
        assertThat(body.getData().get(0).getAppUrl()).isEqualTo(DEFAULT_ADMIN);
    }

    @Test
    void configuredAppUrl_isPreserved() {
        String custom = "http://localhost:8080/admin";
        PortalAppDto app = new PortalAppDto();
        app.setAppName("业务管理系统");
        app.setAppCode("business");
        app.setAppUrl(custom);

        RemoteSysAppApi api = () -> ApiResult.success(List.of(app));

        SsoPortalController controller = new SsoPortalController(api, DEFAULT_ADMIN);
        var body = controller.portalApps().getBody();
        assertThat(body).isNotNull();
        assertThat(body.getData().get(0).getAppUrl()).isEqualTo(custom);
    }

    @Test
    void failureResult_returnsFail() {
        RemoteSysAppApi api = () -> ApiResult.fail("系统服务不可用");
        SsoPortalController controller = new SsoPortalController(api, DEFAULT_ADMIN);
        var resp = controller.portalApps();
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().isSuccess()).isFalse();
        assertThat(resp.getBody().getData()).isNull();
    }

    @Test
    void nullResult_returnsFail() {
        RemoteSysAppApi api = () -> null;
        SsoPortalController controller = new SsoPortalController(api, DEFAULT_ADMIN);
        assertThat(controller.portalApps().getBody()).isNotNull();
        assertThat(controller.portalApps().getBody().isSuccess()).isFalse();
    }
}