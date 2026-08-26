package com.lrcore.gateway.whitelist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 网关安全链“白名单放行”行为回归测试（SAS 接入后白名单失效问题的核心验证）。
 * <p>
 * 背景缺陷：Spring Security 7.x 中 Bearer 令牌校验失败时默认直接走 AuthenticationEntryPoint
 * 返回 401（发生在 permitAll/白名单授权判定之前），导致请求头携带无效令牌时，
 * 即使路径已配置白名单（security.ignore.whites / PERMIT_PATHS）也被 401 拦截。
 * <p>
 * 修复后语义（本测试断言）：
 * <ul>
 *   <li>白名单/公开路径 + 无效令牌（或无令牌） → 放行（响应 200 + PASSED 标记）；</li>
 *   <li>受保护路径 + 无效令牌（或无令牌） → 401 JSON（授权门禁仍然生效）。</li>
 * </ul>
 * 测试环境通过 {@code lrcore.oauth2.issuer=} 关闭 SAS 解码器，
 * 使所有请求中的令牌都走“旧 HS512 解析失败 + SAS 未启用”的失败路径，
 * 精准复现缺陷场景（过期/脏令牌 + 白名单路径）。
 *
 * @ClassName: GatewayWhitelistBehaviorTest
 * @Author: lrcore
 * @Date: 2026/8/21
 * @Version: 1.0
 */
@SpringBootTest(
        classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "server.port=0",
                // 测试不依赖 Nacos / Sentinel / 网关路由（Nacos 导入由 test resources 的 shadow bootstrap.yml 消除）
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.sentinel.enabled=false",
                "spring.cloud.gateway.server.webflux.enabled=false",
                "spring.cloud.gateway.enabled=false",
                // 模拟 Nacos 配置的 security.ignore.whites 白名单（与生产要求一致：
                // SSO/社交登录等无令牌入口必须同时在 AuthFilter 白名单中）
                "security.ignore.whites[0]=/nacos-whitelist/**",
                "security.ignore.whites[1]=/lrcore-auth/api/v1/auth/social/**",
                "security.ignore.whites[2]=/api/v1/auth/captcha",
                // 关闭 SAS 令牌校验（issuer 置空）→ 所有令牌均解码失败，复现缺陷场景
                "lrcore.oauth2.issuer="
        })
class GatewayWhitelistBehaviorTest {

    private static final String INVALID_TOKEN = "garbage.invalid.token";

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer()
                .baseUrl("http://127.0.0.1:" + port)
                .build();
    }

    // ==================== 白名单/公开路径 + 无效令牌 → 必须放行（原缺陷：401） ====================

    /**
     * 认证中心第三方社交登录端点（硬编码 PERMIT_PATHS /lrcore-auth/api/v1/auth/social/**）：
     * 请求头携带无效令牌时仍须放行（用户从第三方扫码登录的前提）。
     */
    @Test
    void invalidTokenOnHardcodedPermitPathShouldPass() {
        client.get().uri("/lrcore-auth/api/v1/auth/social/authorize")
                .header("Authorization", "Bearer " + INVALID_TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(body ->
                        org.assertj.core.api.Assertions.assertThat(body).contains("PASSED"));
    }

    /**
     * 网关自身验证码路由（硬编码 PERMIT_PATHS /api/v1/auth/captcha）：
     * 携带无效令牌时仍须放行。
     */
    @Test
    void invalidTokenOnCaptchaPathShouldPass() {
        client.get().uri("/api/v1/auth/captcha")
                .header("Authorization", "Bearer " + INVALID_TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(body ->
                        org.assertj.core.api.Assertions.assertThat(body).contains("PASSED"));
    }

    /**
     * Nacos 配置的白名单路径（security.ignore.whites=/nacos-whitelist/**）：
     * 携带无效令牌时仍须放行——验证 Nacos 白名单叠加逻辑。
     */
    @Test
    void invalidTokenOnNacosWhitelistPathShouldPass() {
        client.get().uri("/nacos-whitelist/anything")
                .header("Authorization", "Bearer " + INVALID_TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(body ->
                        org.assertj.core.api.Assertions.assertThat(body).contains("PASSED"));
    }

    /**
     * 公开路径 + 无令牌 → 放行（对照场景）。
     */
    @Test
    void noTokenOnPermitPathShouldPass() {
        client.get().uri("/lrcore-auth/api/v1/auth/social/authorize")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(body ->
                        org.assertj.core.api.Assertions.assertThat(body).contains("PASSED"));
    }

    // ==================== 受保护路径 + 无效/无令牌 → 必须 401（授权门禁仍生效） ====================
    // 注意一：生产顺序为 AuthFilter(-200) → 安全链(-100)。非白名单路径的无效/缺失令牌
    //         由 AuthFilter 先行 401（安全链不再触及）；白名单路径的无效令牌则由安全链
    //         “按匿名继续 + 授权放行”（本测试类的核心验证）。
    // 注意二：平台既有约定（ServletUtils.webFluxResponseWriter）——HTTP 状态为 200，
    //         业务状态码放在响应体 code 字段（"401"），前端按 body.code 识别未登录。

    /**
     * 受保护路径 + 无效令牌 → 401（AuthFilter 双轨均失败 → 拦截；修复不能让受保护接口裸奔）。
     */
    @Test
    void invalidTokenOnProtectedPathShould401() {
        client.get().uri("/lrcore-system/some-protected-api")
                .header("Authorization", "Bearer " + INVALID_TOKEN)
                .exchange()
                .expectBody(String.class).value(body ->
                        org.assertj.core.api.Assertions.assertThat(body)
                                .contains("\"code\":\"401\""));
    }

    /**
     * 受保护路径 + 无令牌 → 401（AuthFilter“令牌不能为空”）。
     */
    @Test
    void noTokenOnProtectedPathShould401() {
        client.get().uri("/lrcore-system/some-protected-api")
                .exchange()
                .expectBody(String.class).value(body ->
                        org.assertj.core.api.Assertions.assertThat(body)
                                .contains("\"code\":\"401\""));
    }

    // ==================== 旧链路合法令牌：双轨放行 + 用户头注入 + 内部标记防伪造 ====================

    /**
     * 若依遗留 HS512 令牌（无法经 SAS 授权服务器 RS256 解码）→ AuthFilter 401 拦截
     * —— 网关作为纯 SAS 资源服务器，不再接受旧双 Token 链路令牌。
     */
    @Test
    void legacyHs512TokenOnProtectedPathShould401() {
        java.util.Map<String, Object> claims = new java.util.HashMap<>(4);
        claims.put("user_key", "test-user-key");
        claims.put("user_id", "1");
        claims.put("username", "test-user");
        String legacyToken = io.jsonwebtoken.Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 30L * 60 * 1000))
                .signWith(io.jsonwebtoken.SignatureAlgorithm.HS512,
                        com.lrcore.common.core.constant.TokenConstants.SECRET)
                .compact();

        client.get().uri("/lrcore-system/some-protected-api")
                .header("Authorization", "Bearer " + legacyToken)
                .exchange()
                .expectBody(String.class).value(body ->
                        org.assertj.core.api.Assertions.assertThat(body)
                                .contains("\"code\":\"401\""));
    }

    /**
     * 防伪造回归：白名单直通路径同样必须清除外部伪造的 from-source: inner，
     * 否则外部客户端可借白名单路径携带该头绕过下游资源服务器鉴权。
     */
    @Test
    void forgedInnerHeaderIsStrippedOnWhitelistPath() {
        client.get().uri("/nacos-whitelist/anything")
                .header("from-source", "inner")
                .header("Authorization", "Bearer " + INVALID_TOKEN)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).value(body ->
                        org.assertj.core.api.Assertions.assertThat(body).contains("PASSED"));

        java.util.Map<String, String> forwarded = TestApplication.LAST_FORWARDED_HEADERS.get();
        org.assertj.core.api.Assertions.assertThat(forwarded).isNotNull();
        org.assertj.core.api.Assertions.assertThat(forwarded.keySet()).doesNotContain("from-source");
    }
}
