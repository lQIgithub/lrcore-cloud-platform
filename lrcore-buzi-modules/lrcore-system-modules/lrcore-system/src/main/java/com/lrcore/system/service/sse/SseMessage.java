package com.lrcore.system.service.sse;

/**
 * <p>类模块说明</p>
 *
 * @Describe: Redis Pub/Sub 通道上的 SSE 广播消息。
 *
 * @param userId 目标用户ID（null = 广播给全部在线用户）
 * @param event  事件名，见 {@link SseConstants}
 * @param data   事件载荷 JSON 字符串（发布方已序列化；null 表示空载荷事件）
 * @param origin 发布方实例标识（回环抑制：订阅方忽略本实例发布的消息，
 *               避免同一用户多标签页跨实例连接时重复推送）
 * @Version: 1.0
 */
public record SseMessage(Long userId, String event, String data, String origin) {
}
