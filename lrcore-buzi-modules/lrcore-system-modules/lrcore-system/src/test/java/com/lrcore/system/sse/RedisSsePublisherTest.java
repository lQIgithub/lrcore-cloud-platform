package com.lrcore.system.sse;

import com.lrcore.system.service.sse.RedisSsePublisher;
import com.lrcore.system.service.sse.SseConstants;
import com.lrcore.system.service.sse.SseEmitterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * RedisSsePublisher 单元测试：本地投递 + Redis 跨实例广播（含 Redis 缺失降级）。
 */
class RedisSsePublisherTest {

    private SseEmitterRegistry registry;
    private StringRedisTemplate redisTemplate;
    private ObjectProvider<StringRedisTemplate> redisProvider;
    private RedisSsePublisher publisher;

    @BeforeEach
    void setUp() {
        registry = mock(SseEmitterRegistry.class);
        redisTemplate = mock(StringRedisTemplate.class);
        redisProvider = mock(ObjectProvider.class);
        when(redisProvider.getIfAvailable()).thenReturn(redisTemplate);
        // 与生产环境一致：Jackson 3 JsonMapper（Spring Boot 4 全局序列化语义）
        publisher = new RedisSsePublisher(registry, redisProvider, JsonMapper.builder().build());
    }

    @Test
    void sendToUser_deliversLocallyAndPublishesToRedis() {
        publisher.sendToUser(7L, SseConstants.EVENT_WORKFLOW_TASK, Map.of("taskId", "1"));

        verify(registry).send(eq(7L), eq(SseConstants.EVENT_WORKFLOW_TASK), anyString());
        verify(redisTemplate).convertAndSend(eq(SseConstants.REDIS_CHANNEL), anyString());
    }

    @Test
    void sendToUser_publishedMessageCarriesTargetAndOrigin() {
        publisher.sendToUser(7L, "workflow-task", Map.of("taskId", "1"));

        org.mockito.ArgumentCaptor<String> body = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(redisTemplate).convertAndSend(eq(SseConstants.REDIS_CHANNEL), body.capture());
        assertThat(body.getValue()).contains("\"userId\"").contains("\"origin\"").contains(publisher.getInstanceId());
    }

    @Test
    void sendToUser_withoutRedis_stillDeliversLocally() {
        when(redisProvider.getIfAvailable()).thenReturn(null);

        publisher.sendToUser(7L, "workflow-task", Map.of("taskId", "1"));

        verify(registry).send(eq(7L), eq("workflow-task"), anyString());
    }

    @Test
    void sendToUser_redisFailure_doesNotAffectLocalDelivery() {
        org.mockito.Mockito.doThrow(new IllegalStateException("redis down"))
                .when(redisTemplate).convertAndSend(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());

        publisher.sendToUser(7L, "workflow-task", Map.of("taskId", "1"));

        verify(registry).send(eq(7L), eq("workflow-task"), anyString());
    }

    @Test
    void sendToUser_withNullUserId_broadcasts() {
        publisher.sendToUser(null, SseConstants.EVENT_ONLINE_COUNT, 5);

        verify(registry).sendToAll(eq(SseConstants.EVENT_ONLINE_COUNT), anyString());
        verify(redisTemplate).convertAndSend(eq(SseConstants.REDIS_CHANNEL), anyString());
    }

    @Test
    void sendToUsers_withNullOrEmpty_isNoOp() {
        publisher.sendToUsers(null, "workflow-task", Map.of());
        publisher.sendToUsers(List.of(), "workflow-task", Map.of());

        verifyNoInteractions(registry, redisTemplate);
    }

    @Test
    void sendToUsers_publishesOncePerUser() {
        publisher.sendToUsers(List.of(1L, 2L), "workflow-task", Map.of("taskId", "1"));

        verify(registry).send(eq(1L), eq("workflow-task"), anyString());
        verify(registry).send(eq(2L), eq("workflow-task"), anyString());
        org.mockito.Mockito.verify(redisTemplate, org.mockito.Mockito.times(2))
                .convertAndSend(eq(SseConstants.REDIS_CHANNEL), anyString());
    }

    @Test
    void instanceId_isStableAndNotEmpty() {
        assertThat(publisher.getInstanceId()).isNotBlank();
        assertThat(publisher.getInstanceId()).isEqualTo(publisher.getInstanceId());
    }
}
