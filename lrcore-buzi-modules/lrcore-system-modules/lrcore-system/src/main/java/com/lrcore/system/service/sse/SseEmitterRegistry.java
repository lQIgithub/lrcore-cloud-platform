package com.lrcore.system.service.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSE 连接登记表：维护 userId → 该用户在本实例的全部 SseEmitter 连接。
 *
 * <p>说明：
 * <ul>
 *   <li>每用户连接数超过上限时淘汰最旧连接（complete 触发前端重连），防止单用户连接泄漏；</li>
 *   <li>连接超时/错误/完成均自动从表中移除，发送失败（IOException 等）的连接即时剔除；</li>
 *   <li>本表只负责【本实例】投递；跨实例广播由 RedisSsePublisher + SseRedisSubscriber 完成。</li>
 * </ul>
 *
 * @Version: 1.0
 */
@Slf4j
@Component
public class SseEmitterRegistry {

    /** userId → 该用户的 Emitter 集合（LinkedHashSet 保持连接建立顺序，便于 LRU 淘汰） */
    private final Map<Long, Set<SseEmitter>> connections = new ConcurrentHashMap<>();

    private final long emitterTimeoutMs;
    private final int maxConnectionsPerUser;

    public SseEmitterRegistry(
            @Value("${" + SseConstants.PROP_EMITTER_TIMEOUT_MS + ":" + SseConstants.DEFAULT_EMITTER_TIMEOUT_MS + "}") long emitterTimeoutMs,
            @Value("${" + SseConstants.PROP_MAX_CONNECTIONS_PER_USER + ":" + SseConstants.DEFAULT_MAX_CONNECTIONS_PER_USER + "}") int maxConnectionsPerUser) {
        this.emitterTimeoutMs = emitterTimeoutMs;
        this.maxConnectionsPerUser = maxConnectionsPerUser;
    }

    /**
     * 为用户建立 SSE 连接并登记。
     * 超出单用户连接上限时淘汰最旧的连接（complete 后由前端自动重连）。
     */
    public SseEmitter register(Long userId) {
        Set<SseEmitter> userConnections = connections.computeIfAbsent(
                userId, key -> Collections.synchronizedSet(new LinkedHashSet<>()));

        SseEmitter emitter = createEmitter();
        synchronized (userConnections) {
            while (userConnections.size() >= maxConnectionsPerUser) {
                SseEmitter evicted = userConnections.iterator().next();
                userConnections.remove(evicted);
                log.info("SSE 单用户连接数达到上限（{}），淘汰用户 {} 的最旧连接", maxConnectionsPerUser, userId);
                evicted.complete();
            }
            userConnections.add(emitter);
        }

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            log.debug("SSE 连接超时，userId={}", userId);
            remove(userId, emitter);
            emitter.complete();
        });
        emitter.onError(throwable -> {
            log.debug("SSE 连接异常，userId={}", userId, throwable);
            remove(userId, emitter);
        });
        return emitter;
    }

    /**
     * 创建 Emitter 实例。
     * protected 可重写，便于单元测试注入可断言的 Emitter。
     */
    protected SseEmitter createEmitter() {
        return new SseEmitter(emitterTimeoutMs);
    }

    /**
     * 向目标用户的全部连接推送事件（本实例无连接时为空操作）。
     *
     * @param userId 目标用户ID
     * @param event  事件名（null 时使用默认 message 事件）
     * @param data   事件载荷 JSON 字符串（null 表示只发事件名）
     */
    public void send(Long userId, String event, String data) {
        Set<SseEmitter> userConnections = connections.get(userId);
        if (userConnections == null || userConnections.isEmpty()) {
            return;
        }
        List<SseEmitter> dead = new ArrayList<>();
        synchronized (userConnections) {
            for (SseEmitter emitter : userConnections) {
                try {
                    emitter.send(buildEvent(event, data));
                } catch (Exception e) {
                    dead.add(emitter);
                }
            }
            dead.forEach(userConnections::remove);
        }
        dead.forEach(SseEmitter::complete);
    }

    /** 向全部在线用户推送事件 */
    public void sendToAll(String event, String data) {
        for (Long userId : connections.keySet()) {
            send(userId, event, data);
        }
    }

    /**
     * 向全部在线用户发送心跳注释帧（":ping"），防止中间代理断开空闲连接。
     *
     * @return 当前在线用户数
     */
    public int heartbeat() {
        int onlineUsers = 0;
        for (Map.Entry<Long, Set<SseEmitter>> entry : connections.entrySet()) {
            Set<SseEmitter> userConnections = entry.getValue();
            if (userConnections.isEmpty()) {
                continue;
            }
            onlineUsers++;
            List<SseEmitter> dead = new ArrayList<>();
            synchronized (userConnections) {
                for (SseEmitter emitter : userConnections) {
                    try {
                        emitter.send(SseEmitter.event().comment("ping"));
                    } catch (Exception e) {
                        dead.add(emitter);
                    }
                }
                dead.forEach(userConnections::remove);
            }
            dead.forEach(SseEmitter::complete);
        }
        return onlineUsers;
    }

    /** 目标用户在本实例是否有活动连接 */
    public boolean hasConnections(Long userId) {
        Set<SseEmitter> userConnections = connections.get(userId);
        return userConnections != null && !userConnections.isEmpty();
    }

    /** 本实例当前在线用户数 */
    public int onlineUserCount() {
        int count = 0;
        for (Set<SseEmitter> userConnections : connections.values()) {
            if (!userConnections.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /** 本实例当前全部连接数（含同一用户多标签页） */
    public int totalConnectionCount() {
        int count = 0;
        for (Set<SseEmitter> userConnections : connections.values()) {
            count += userConnections.size();
        }
        return count;
    }

    private void remove(Long userId, SseEmitter emitter) {
        Set<SseEmitter> userConnections = connections.get(userId);
        if (userConnections == null) {
            return;
        }
        synchronized (userConnections) {
            userConnections.remove(emitter);
            if (userConnections.isEmpty()) {
                connections.remove(userId, userConnections);
            }
        }
    }

    private static SseEmitter.SseEventBuilder buildEvent(String event, String data) {
        SseEmitter.SseEventBuilder builder = SseEmitter.event();
        if (event != null) {
            builder.name(event);
        }
        if (data != null) {
            builder.data(data);
        }
        return builder;
    }
}
