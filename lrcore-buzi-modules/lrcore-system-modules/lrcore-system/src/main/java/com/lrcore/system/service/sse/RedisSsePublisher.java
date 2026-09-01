package com.lrcore.system.service.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.UUID;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSE 消息发布器默认实现：本地直投 + Redis Pub/Sub 跨实例广播。
 *
 * <p>投递策略：
 * <ol>
 *   <li>本地投递：目标用户在本实例有连接 → 直接经 {@link SseEmitterRegistry} 发送；</li>
 *   <li>跨实例广播：消息发布到 Redis 通道 {@link SseConstants#REDIS_CHANNEL}，
 *       各实例的 SseRedisSubscriber 负责投递到本实例连接；</li>
 *   <li>回环抑制：订阅方忽略本实例（origin 相同）发布的消息，
 *       避免同一用户多标签页跨实例连接时收到重复推送。</li>
 * </ol>
 *
 * <p>Redis 不可用时仅本地投递生效（log.warn，绝不影响调用方主流程）。
 *
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisSsePublisher implements SsePublisher {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;
    private final ObjectMapper objectMapper;

    /** 实例标识（本实例广播消息的回环抑制依据） */
    private final String instanceId = UUID.randomUUID().toString();

    @Override
    public void sendToUser(Long userId, String event, Object data) {
        if (userId == null) {
            broadcast(event, data);
            return;
        }
        String dataJson = toJson(data);
        sseEmitterRegistry.send(userId, event, dataJson);
        publish(new SseMessage(userId, event, dataJson, instanceId));
    }

    @Override
    public void sendToUsers(Collection<Long> userIds, String event, Object data) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        for (Long userId : userIds) {
            sendToUser(userId, event, data);
        }
    }

    @Override
    public void broadcast(String event, Object data) {
        String dataJson = toJson(data);
        sseEmitterRegistry.sendToAll(event, dataJson);
        publish(new SseMessage(null, event, dataJson, instanceId));
    }

    /** 实例标识（供订阅方做回环抑制） */
    public String getInstanceId() {
        return instanceId;
    }

    private void publish(SseMessage message) {
        StringRedisTemplate redisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (redisTemplate == null) {
            log.debug("StringRedisTemplate 不可用，跳过 SSE 跨实例广播（仅本实例本地投递生效）");
            return;
        }
        try {
            redisTemplate.convertAndSend(SseConstants.REDIS_CHANNEL, objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.warn("SSE 消息发布到 Redis 失败（跨实例推送不可用，本地投递不受影响）：{}", e.getMessage());
        }
    }

    private String toJson(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JacksonException e) {
            // Jackson 3 序列化失败抛运行时 JacksonException（不再抛受检 JsonProcessingException）
            log.error("SSE 事件载荷序列化失败，丢弃该事件：{}", e.getMessage());
            return null;
        }
    }
}
