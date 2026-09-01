package com.lrcore.system.controller.sse;

import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.system.service.sse.SseEmitterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSE 实时推送连接控制器。
 *
 * <p>前端（lrcore-cloud-admin-ui useSse）携带 Bearer 令牌 GET 本端点建立长连接，
 * 连接建立后服务端经该通道推送 workflow-task / workflow-instance / notice / dict 等事件。
 *
 * <p>鉴权链路：网关 AuthFilter 验签后注入 user_id 等标头 → lrcore-common-web HeaderInterceptor
 * 写入 SecurityContextHolder → 本控制器经 SecurityUtils 读取当前用户。
 *
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
@Schema(description = "SSE 实时推送连接控制器")
public class SseConnectController extends BaseController {

    private final SseEmitterRegistry sseEmitterRegistry;

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "建立 SSE 长连接", description = "返回以当前登录用户为载体的 Server-Sent Events 流")
    public SseEmitter connect() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new ServiceException("用户未登录，无法建立 SSE 连接");
        }
        logger.info("SSE 连接建立，userId={}", userId);
        return sseEmitterRegistry.register(userId);
    }
}
