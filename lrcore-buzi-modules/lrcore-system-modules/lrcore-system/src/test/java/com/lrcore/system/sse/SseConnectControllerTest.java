package com.lrcore.system.sse;

import com.lrcore.common.core.context.SecurityContextHolder;
import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.system.controller.sse.SseConnectController;
import com.lrcore.system.service.sse.SseEmitterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SseConnectController 单元测试：鉴权（登录上下文）与连接登记。
 */
class SseConnectControllerTest {

    private SseEmitterRegistry registry;
    private SseConnectController controller;

    private SseConnectController newController() {
        registry = new SseEmitterRegistry(10_000L, 5);
        return new SseConnectController(registry);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setLocalMap(new HashMap<>());
    }

    @Test
    void connect_withLoggedInUser_registersAndReturnsEmitter() {
        SseConnectController localController = newController();
        SecurityContextHolder.setUserId(42L);

        SseEmitter emitter = localController.connect();

        assertThat(emitter).isNotNull();
        assertThat(registry.hasConnections(42L)).isTrue();
        assertThat(registry.onlineUserCount()).isEqualTo(1);
    }

    @Test
    void connect_withoutLoggedInUser_throwsServiceException() {
        SseConnectController localController = newController();

        // ServiceException 的提示存于 errorMessage 字段（未传给 super，getMessage() 为 null）
        assertThatThrownBy(localController::connect)
                .isInstanceOf(ServiceException.class)
                .matches(e -> ((ServiceException) e).getErrorMessage().contains("未登录"));
        assertThat(registry.onlineUserCount()).isZero();
    }
}
