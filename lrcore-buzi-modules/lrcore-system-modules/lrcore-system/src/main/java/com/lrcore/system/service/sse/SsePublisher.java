package com.lrcore.system.service.sse;

import java.util.Collection;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSE 消息发布器。业务代码（工作流事件、通知、在线数等）统一经此接口推送，
 *            无需关心连接是否在本实例、跨实例广播细节。
 *
 * @Version: 1.0
 */
public interface SsePublisher {

    /**
     * 向指定用户推送事件（其全部连接；本实例无连接时经 Redis 广播给其他实例）。
     * userId 为 null 时退化为 {@link #broadcast(String, Object)}。
     *
     * @param userId 目标用户ID
     * @param event  事件名，见 {@link SseConstants}
     * @param data   事件载荷（任意可 JSON 序列化对象；Long 型 ID 建议自行转 String）
     */
    void sendToUser(Long userId, String event, Object data);

    /**
     * 向多个用户推送同一事件。
     *
     * @param userIds 目标用户ID集合（null/空 时不推送）
     */
    void sendToUsers(Collection<Long> userIds, String event, Object data);

    /**
     * 广播事件给全部在线用户。
     *
     * @param event 事件名
     * @param data  事件载荷
     */
    void broadcast(String event, Object data);
}
