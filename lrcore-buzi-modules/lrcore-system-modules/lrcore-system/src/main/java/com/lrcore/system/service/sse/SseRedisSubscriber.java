package com.lrcore.system.service.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import tools.jackson.databind.ObjectMapper;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSE 跨实例广播订阅器：监听 Redis 通道 {@link SseConstants#REDIS_CHANNEL}，
 *            将其他实例发布的事件投递到本实例连接表。
 *
 * <p>回环抑制：忽略本实例（origin 与 localInstanceId 相同）发布的消息——
 * 该消息在发布时已做过本地投递，避免同一用户多标签页跨实例连接时重复推送。
 *
 * @Version: 1.0
 */
@Slf4j
public class SseRedisSubscriber implements MessageListener {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;
    private final String localInstanceId;

    public SseRedisSubscriber(SseEmitterRegistry sseEmitterRegistry, ObjectMapper objectMapper, String localInstanceId) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
        this.localInstanceId = localInstanceId;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        SseMessage sseMessage;
        try {
            sseMessage = objectMapper.readValue(message.getBody(), SseMessage.class);
        } catch (Exception e) {
            log.warn("SSE 广播消息解析失败，已忽略：{}", e.getMessage());
            return;
        }
        if (sseMessage == null || sseMessage.event() == null) {
            return;
        }
        if (localInstanceId != null && localInstanceId.equals(sseMessage.origin())) {
            return;
        }
        if (sseMessage.userId() == null) {
            sseEmitterRegistry.sendToAll(sseMessage.event(), sseMessage.data());
        } else {
            sseEmitterRegistry.send(sseMessage.userId(), sseMessage.event(), sseMessage.data());
        }
    }
}
