package com.lrcore.auth.social;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 微信开放平台本地 Mock —— 仅实现 lrcore-common-auth 微信扫码客户端实际调用的两个端点：
 * <ul>
 *   <li>GET /sns/oauth2/access_token?appid=&secret=&code=&grant_type= —— 授权码换令牌；</li>
 *   <li>GET /sns/userinfo?access_token=&openid= —— 拉取用户信息。</li>
 * </ul>
 * 测试用例通过 {@link #registerCode(String, String)} 预注册 code→openid 映射；
 * 未注册的 code 返回微信标准错误响应 {@code {"errcode":40029,"errmsg":"invalid code"}}。
 * @ClassName: MockWeChatServer
 * @Author: Qi Liu
 * @Date: 2026/8/20
 * @Version: 1.0
 */
public final class MockWeChatServer implements AutoCloseable {

    private final HttpServer server;

    private final Map<String, String> codeToOpenId = new ConcurrentHashMap<>();

    public MockWeChatServer() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.server.createContext("/sns/oauth2/access_token", this::handleToken);
        this.server.createContext("/sns/userinfo", this::handleUserInfo);
        this.server.start();
    }

    public int port() {
        return this.server.getAddress().getPort();
    }

    /** 注册一个可换取指定 openid 的授权码。 */
    public void registerCode(String code, String openId) {
        this.codeToOpenId.put(code, openId);
    }

    @Override
    public void close() {
        this.server.stop(0);
    }

    // ==================== 端点实现 ====================

    private void handleToken(HttpExchange exchange) throws IOException {
        String code = query(exchange, "code");
        String openId = code == null ? null : this.codeToOpenId.remove(code); // 一次性消费
        String body;
        if (openId == null) {
            body = "{\"errcode\":40029,\"errmsg\":\"invalid code\"}";
        } else {
            body = "{\"access_token\":\"at-" + openId + "\",\"expires_in\":7200,"
                    + "\"refresh_token\":\"rt-" + openId + "\",\"openid\":\"" + openId + "\","
                    + "\"scope\":\"snsapi_login\",\"unionid\":\"union-" + openId + "\"}";
        }
        respond(exchange, body);
    }

    private void handleUserInfo(HttpExchange exchange) throws IOException {
        String openId = query(exchange, "openid");
        String body;
        if (openId == null || openId.isBlank()) {
            body = "{\"errcode\":40029,\"errmsg\":\"invalid openid\"}";
        } else {
            body = "{\"openid\":\"" + openId + "\",\"unionid\":\"union-" + openId + "\","
                    + "\"nickname\":\"微信用户-" + openId + "\","
                    + "\"headimgurl\":\"https://wx.example.com/avatar/" + openId + ".png\","
                    + "\"sex\":1,\"country\":\"CN\",\"province\":\"GD\",\"city\":\"SZ\"}";
        }
        respond(exchange, body);
    }

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json;charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String query(HttpExchange exchange, String name) {
        String rawQuery = exchange.getRequestURI().getRawQuery();
        if (rawQuery == null) {
            return null;
        }
        for (String pair : rawQuery.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0 && pair.substring(0, idx).equals(name)) {
                return URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
