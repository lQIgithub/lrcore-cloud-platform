package com.lrcore.system.service.sse;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSE 实时推送通道常量：事件名契约、Redis 广播通道、配置键与默认值。
 *
 * <p>事件名契约（前端同名使用，见 lrcore-cloud-admin-ui/src/composables/sse）：
 * <ul>
 *   <li>{@link #EVENT_DICT}：字典变更，载荷 {dictCode, timestamp}</li>
 *   <li>{@link #EVENT_ONLINE_COUNT}：在线用户数，载荷为数字</li>
 *   <li>{@link #EVENT_NOTICE}：新通知，载荷 {id, title, type, publishTime}</li>
 *   <li>{@link #EVENT_NOTICE_REVOKE}：通知撤回，载荷 {id}</li>
 *   <li>{@link #EVENT_WORKFLOW_TASK}：工作流任务事件（本次交付），载荷见 WorkflowSseNotifierImpl</li>
 *   <li>{@link #EVENT_WORKFLOW_INSTANCE}：工作流实例事件（本次交付），载荷见 WorkflowSseNotifierImpl</li>
 * </ul>
 *
 * @Version: 1.0
 */
public final class SseConstants {

    private SseConstants() {
    }

    /** 事件名：字典变更（前端既有场景） */
    public static final String EVENT_DICT = "dict";

    /** 事件名：在线用户数（前端既有场景） */
    public static final String EVENT_ONLINE_COUNT = "online-count";

    /** 事件名：新通知（前端既有场景） */
    public static final String EVENT_NOTICE = "notice";

    /** 事件名：通知撤回（前端既有场景） */
    public static final String EVENT_NOTICE_REVOKE = "notice-revoke";

    /** 事件名：工作流任务事件（任务创建/指派/完成/删除） */
    public static final String EVENT_WORKFLOW_TASK = "workflow-task";

    /** 事件名：工作流实例事件（实例启动/完成/挂起/激活/终止） */
    public static final String EVENT_WORKFLOW_INSTANCE = "workflow-instance";

    /** Redis Pub/Sub 通道：SSE 多实例广播 */
    public static final String REDIS_CHANNEL = "lrcore:sse:broadcast";

    /** 配置键：SSE 连接（Emitter）超时，毫秒 */
    public static final String PROP_EMITTER_TIMEOUT_MS = "lrcore.sse.emitter-timeout-ms";

    /** 默认连接超时：30 分钟（超时后前端自动重连） */
    public static final long DEFAULT_EMITTER_TIMEOUT_MS = 30L * 60 * 1000;

    /** 配置键：单用户最大并发连接数（多标签页场景） */
    public static final String PROP_MAX_CONNECTIONS_PER_USER = "lrcore.sse.max-connections-per-user";

    /** 默认单用户最大并发连接数 */
    public static final int DEFAULT_MAX_CONNECTIONS_PER_USER = 10;

    /** 配置键：心跳间隔，毫秒 */
    public static final String PROP_HEARTBEAT_MS = "lrcore.sse.heartbeat-ms";

    /** 默认心跳间隔：30 秒（防止中间代理断开空闲连接） */
    public static final long DEFAULT_HEARTBEAT_MS = 30L * 1000;
}
