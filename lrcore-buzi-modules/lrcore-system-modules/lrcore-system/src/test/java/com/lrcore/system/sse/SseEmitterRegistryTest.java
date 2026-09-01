package com.lrcore.system.sse;

import com.lrcore.system.service.sse.SseEmitterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SseEmitterRegistry 单元测试：登记/投递/淘汰/心跳。
 */
class SseEmitterRegistryTest {

    /** 记录已发送事件的 Emitter，便于断言 */
    static class RecordingEmitter extends SseEmitter {

        final List<SseEmitter.SseEventBuilder> sent = new CopyOnWriteArrayList<>();

        RecordingEmitter() {
            super(10_000L);
        }

        @Override
        public void send(SseEmitter.SseEventBuilder builder) {
            sent.add(builder);
        }
    }

    static class TestableRegistry extends SseEmitterRegistry {

        TestableRegistry(int maxConnectionsPerUser) {
            super(10_000L, maxConnectionsPerUser);
        }

        @Override
        protected SseEmitter createEmitter() {
            return new RecordingEmitter();
        }
    }

    private TestableRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new TestableRegistry(2);
    }

    @Test
    void register_and_send_deliversToUserConnections() {
        SseEmitter emitter = registry.register(1L);

        registry.send(1L, "workflow-task", "{\"taskId\":\"9\"}");

        assertThat(((RecordingEmitter) emitter).sent).hasSize(1);
        assertThat(registry.hasConnections(1L)).isTrue();
        assertThat(registry.onlineUserCount()).isEqualTo(1);
    }

    @Test
    void send_toUserWithoutConnection_isNoOp() {
        registry.send(99L, "workflow-task", "{}");

        assertThat(registry.onlineUserCount()).isZero();
    }

    @Test
    void register_evictsOldestWhenPerUserCapExceeded() {
        SseEmitter first = registry.register(1L);
        SseEmitter second = registry.register(1L);
        SseEmitter third = registry.register(1L);

        assertThat(registry.totalConnectionCount()).isEqualTo(2);
        assertThat(first).isNotSameAs(third);
        assertThat(second).isNotSameAs(third);
        // 被淘汰的最旧连接仍可从表中找回的第二个/第三个连接中排除
        registry.send(1L, "workflow-task", "{}");
        assertThat(((RecordingEmitter) first).sent).isEmpty();
        assertThat(((RecordingEmitter) second).sent).hasSize(1);
        assertThat(((RecordingEmitter) third).sent).hasSize(1);
    }

    @Test
    void sendToAll_reachesAllOnlineUsers() {
        SseEmitter userA = registry.register(1L);
        SseEmitter userB = registry.register(2L);

        registry.sendToAll("online-count", "42");

        assertThat(((RecordingEmitter) userA).sent).hasSize(1);
        assertThat(((RecordingEmitter) userB).sent).hasSize(1);
    }

    @Test
    void heartbeat_sendsCommentFrameAndCountsOnlineUsers() {
        SseEmitter userA = registry.register(1L);
        SseEmitter userB = registry.register(2L);

        int onlineUsers = registry.heartbeat();

        assertThat(onlineUsers).isEqualTo(2);
        assertThat(((RecordingEmitter) userA).sent).hasSize(1);
        assertThat(((RecordingEmitter) userB).sent).hasSize(1);
    }

    @Test
    void emptyRegistry_reportsZeroOnline() {
        assertThat(registry.onlineUserCount()).isZero();
        assertThat(registry.totalConnectionCount()).isZero();
        assertThat(registry.heartbeat()).isZero();
    }
}
