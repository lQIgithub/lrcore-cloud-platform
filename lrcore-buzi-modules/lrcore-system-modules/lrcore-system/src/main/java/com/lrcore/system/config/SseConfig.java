package com.lrcore.system.config;

import com.lrcore.system.service.sse.RedisSsePublisher;
import com.lrcore.system.service.sse.SseConstants;
import com.lrcore.system.service.sse.SseEmitterRegistry;
import com.lrcore.system.service.sse.SseRedisSubscriber;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSE 通道装配：Redis 广播订阅容器 + 周期心跳。
 *
 * <p>连接端点见 SseConnectController（GET /api/v1/sse/connect），
 * 本地投递/跨实例广播见 service.sse 包。
 *
 * @Version: 1.0
 */
@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SseConfig {

    private final SseEmitterRegistry sseEmitterRegistry;

    /** SSE 跨实例广播订阅器（由 RedisSsePublisher 的实例标识做回环抑制） */
    @Bean
    public SseRedisSubscriber sseRedisSubscriber(RedisSsePublisher redisSsePublisher) {
        return new SseRedisSubscriber(sseEmitterRegistry, redisSsePublisher.getInstanceId());
    }

    /** SSE 跨实例广播订阅容器（订阅 Redis 通道 lrcore:sse:broadcast） */
    @Bean
    public RedisMessageListenerContainer sseRedisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory, SseRedisSubscriber sseRedisSubscriber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(sseRedisSubscriber, new ChannelTopic(SseConstants.REDIS_CHANNEL));
        return container;
    }

    /** 周期心跳：向全部连接发送注释帧（:ping），防止中间代理断开空闲连接 */
    @Scheduled(
            fixedDelayString = "${" + SseConstants.PROP_HEARTBEAT_MS + ":" + SseConstants.DEFAULT_HEARTBEAT_MS + "}",
            initialDelayString = "${" + SseConstants.PROP_HEARTBEAT_MS + ":" + SseConstants.DEFAULT_HEARTBEAT_MS + "}")
    public void sseHeartbeat() {
        int onlineUsers = sseEmitterRegistry.heartbeat();
        if (onlineUsers > 0) {
            log.debug("SSE 心跳已发送，当前在线用户数：{}", onlineUsers);
        }
    }
}
