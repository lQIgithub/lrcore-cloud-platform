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
 * @Describe: QQ 互联（QQ Connect）本地 Mock —— 按真实 API 响应形态实现
 *            lrcore-common-auth QQ 客户端实际调用的三个端点：
 * <ul>
 *   <li>GET /oauth2.0/token?code=...&amp;... —— 授权码换令牌，
 *       默认返回 URL 编码查询串（不含 openid）：
 *       {@code access_token=..&expires_in=7200&refresh_token=..}，
 *       未注册的 code 返回 {@code error=100010&error_hint=invalid code}；</li>
 *   <li>GET /oauth2.0/me?access_token=... —— 查询 openid，
 *       返回 {@code {"client_id":"..","openid":".."}}，
 *       无效令牌返回 {@code {"error":"invalid_token","error_description":".."}}；</li>
 *   <li>GET /user/get_user_info?access_token=&openid=... —— 拉取用户信息，
 *       返回扁平 JSON（ret/nickname/figureurl_qq_2...），无效返回 ret 非 0。</li>
 * </ul>
 * 测试用例通过 {@link #registerCode(String, String)} 预注册 code→openid 映射（令牌一次性消费）。
 * @ClassName: MockQqServer
 * @Author: Qi Liu
 * @Date: 2026/8/21
 * @Version: 1.0
 */
public final class MockQqServer implements AutoCloseable {

    private final HttpServer server;

    /** code → openid（换令牌时一次性消费） */
    private final Map<String, String> codeToOpenId = new ConcurrentHashMap<>();

    /** access_token → openid（令牌交换成功后注册） */
    private final Map<String, String> tokenToOpenId = new ConcurrentHashMap<>();

    public MockQqServer() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        this.server.createContext("/oauth2.0/token", this::handleToken);
        this.server.createContext("/oauth2.0/me", this::handleMe);
        this.server.createContext("/user/get_user_info", this::handleUserInfo);
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

    /**
     * 授权码换令牌（真实默认形态：查询串，不含 openid）。
     */
    private void handleToken(HttpExchange exchange) throws IOException {
        String code = query(exchange, "code");
        String openId = code == null ? null : this.codeToOpenId.remove(code); // 一次性消费
        String body;
        if (openId == null) {
            body = "error=100010&error_hint=invalid+code";
        } else {
            String accessToken = "at-" + openId;
            this.tokenToOpenId.put(accessToken, openId);
            body = "access_token=" + accessToken + "&expires_in=7200&refresh_token=rt-" + openId;
        }
        respond(exchange, body);
    }

    /**
     * 查询 openid（QQ 独有步骤，openid 不随令牌返回）。
     */
    private void handleMe(HttpExchange exchange) throws IOException {
        String token = query(exchange, "access_token");
        String openId = token == null ? null : this.tokenToOpenId.get(token);
        String body;
        if (openId == null) {
            body = "{\"error\":\"invalid_token\",\"error_description\":\"access token expired\"}";
        } else {
            body = "{\"client_id\":\"qq-test-appid\",\"openid\":\"" + openId + "\"}";
        }
        respondJson(exchange, body);
    }

    /**
     * 用户信息（扁平 JSON，openId 不在响应体内）。
     */
    private void handleUserInfo(HttpExchange exchange) throws IOException {
        String token = query(exchange, "access_token");
        String openId = query(exchange, "openid");
        String boundOpenId = token == null ? null : this.tokenToOpenId.get(token);
        String body;
        if (openId == null || openId.isBlank() || !openId.equals(boundOpenId)) {
            body = "{\"ret\":100010,\"msg\":\"access token invalid\",\"data\":[]}";
        } else {
            body = "{\"ret\":0,\"is_lost\":0,\"nickname\":\"QQ用户-" + openId + "\",\"gender\":\"M\","
                    + "\"province\":\"GD\",\"city\":\"SZ\","
                    + "\"figureurl\":\"https://qq.example.com/" + openId + "/0.png\","
                    + "\"figureurl_qq_1\":\"https://qq.example.com/" + openId + "/1.png\","
                    + "\"figureurl_qq_2\":\"https://qq.example.com/" + openId + "/2.png\"}";
        }
        respondJson(exchange, body);
    }

    private static void respond(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html;charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void respondJson(HttpExchange exchange, String body) throws IOException {
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
