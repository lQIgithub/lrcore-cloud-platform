package com.lrcore.auth.sso;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lrcore.auth.config.LrcoreSsoHostConfiguration;
import com.lrcore.auth.config.LrcoreSsoSecurityConfig;
import com.lrcore.auth.controller.SsoCaptchaController;
import com.lrcore.auth.controller.SsoLoginController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.login.LoginUserDto;
import com.lrcore.common.redis.service.RedisService;
import com.lrcore.system.api.RemoteUserApi;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.SignedJWT;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.reactive.server.ExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SSO 单点登录全链路集成测试（真实 HTTP 打到真实 Tomcat + 真实 Spring Authorization Server）。
 * <p>
 * 真实部件：SAS 协议端点（/oauth2/authorize、/oauth2/token、/oauth2/jwks）、
 * LrcoreSsoSecurityConfig 安全链（登录过滤链 + 会话 + 登出）、LrcoreSsoLoginFilter、
 * PasswordLrcoreAuthenticator 策略链（验证码/防暴破/锁定/BCrypt）、LrcoreUserSource
 * （Feign 桩）、LrcoreTokenCustomizer（claims 契约）、JDBC 持久化（H2 内存库，官方 DDL）、
 * LrcoreBackChannelLogoutPublisher 的授权撤销（JdbcLrcoreLogoutTokenRevoker）。
 * 替身部件：Redis（内存 Map，验证码值经 {@link #TEST_STATE} 观测）/ RemoteUserApi（Feign 桩）。
 * <p>
 * 覆盖（按 SSO 流程计划 4.2-D）：
 * <ol>
 *   <li>未认证 /oauth2/authorize → 302 统一登录页（SSO 入口）；</li>
 *   <li>登录页渲染（CSRF 注入）+ 验证码（uuid + base64 PNG，Redis 约定写入）；</li>
 *   <li>错误验证码 / 错误密码 → 302 /login?error=...（策略链生效）；</li>
 *   <li>正确登录 → 302 回跳原 /oauth2/authorize（SavedRequest 续接）；</li>
 *   <li>带 AS 会话 /oauth2/authorize → 直接 302 code（<b>SSO 免登核心断言</b>）；</li>
 *   <li>code + code_verifier 换令牌（公共客户端 PKCE；错误 verifier 拒绝）；
 *       [SAS 7.1] 公共客户端授权码流程不签发 refresh_token（OAuth 2.1 / RFC 9700 方向），
 *       SPA 令牌过期改走 prompt=none 静默再授权；</li>
 *   <li>访问令牌 JWKS 验签 + claims 契约（iss / sub=user_id / username / tenant_id）；</li>
 *   <li>登出 → 302 /login?logout=1 + 会话失效 + 该用户全部授权被撤销（SLO 闭环）。</li>
 * </ol>
 * 测试间共享服务端状态（H2/会话/令牌），按 @Order 串行执行单一用户故事。
 *
 * @ClassName SsoFlowIntegrationTest
 * @Author lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@SpringBootTest(
        classes = SsoFlowIntegrationTest.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.banner-mode=off",
                "logging.level.root=WARN",
                "lrcore.auth.enabled=true",
                "lrcore.auth.issuer=http://localhost:10802",
                "lrcore.auth.jdbc.enabled=true",
                // OAuth2 三表官方 DDL（SAS jar 内，方言中立 H2 兼容），上下文启动期初始化
                "spring.sql.init.mode=always",
                "spring.sql.init.schema-locations=classpath:org/springframework/security/oauth2/server/authorization/client/oauth2-registered-client-schema.sql,"
                        + "classpath:org/springframework/security/oauth2/server/authorization/oauth2-authorization-schema.sql,"
                        + "classpath:org/springframework/security/oauth2/server/authorization/oauth2-authorization-consent-schema.sql",
                "lrcore.auth.oidc-logout.enabled=true",
                "lrcore.auth.oidc-logout.issuer=http://localhost:10802",
                "encrypt.aes.enabled=false",
                "jasypt.encryptor.password=test-jasypt-password",
                "truelicense.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.service-registry.auto-registration.enabled=false"
        })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SsoFlowIntegrationTest {

    static final String ADMIN_PASSWORD = "Lr@123456.";
    static final String ADMIN_BCRYPT = new BCryptPasswordEncoder().encode(ADMIN_PASSWORD);
    static final long ADMIN_USER_ID = 7264590000000000071L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** PKCE 参数（整个测试类共用一组 verifier/challenge）。 */
    private final String codeVerifier = randomPkceVerifier();
    private final String codeChallenge = sha256Base64Url(codeVerifier);

    private static final String REDIRECT_URI = "http://localhost:3000/sso/oauth-callback";
    private static final String CLIENT_ID = "web-admin-spa";
    private static final String ISSUER = "http://localhost:10802";

    @LocalServerPort
    private int port;

    private WebTestClient client;
    private final CookieJar cookieJar = new CookieJar();

    /** 登录成功后保留的访问令牌（JWKS 验签断言用）。 */
    private String accessToken;

    /** 直连 OAuth2 授权表的 JdbcTemplate（SLO 撤销断言用）。 */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        this.client = WebTestClient.bindToServer()
                .baseUrl("http://127.0.0.1:" + port)
                .responseTimeout(Duration.ofSeconds(15))
                .build();
    }

    // ==================== 1. SSO 入口 ====================

    @Test
    @Order(1)
    @DisplayName("入口：未认证 /oauth2/authorize → 302 统一登录页")
    void authorize_without_session_redirects_to_login() {
        cookieJar.clear();
        ExchangeResult resp = browserGet(authorizeUri("state-entry"));
        String location = locationOf(resp);
        assertThat(location).contains("/login");
        assertThat(location).doesNotContain("oauth-callback");
    }

    @Test
    @Order(2)
    @DisplayName("登录页：渲染成功且注入 CSRF 令牌（占位符已替换）")
    void login_page_rendered_with_csrf() {
        String html = browserGetBody("/login");
        assertThat(html).isNotNull();
        assertThat(html).doesNotContain("__CSRF_TOKEN__", "__ERROR_DISPLAY__");
        assertThat(csrfTokenOf(html)).isNotBlank();
    }

    @Test
    @Order(3)
    @DisplayName("验证码：返回 uuid + base64 PNG，且按约定写入 Redis（captcha_codes:{uuid}）")
    void captcha_returns_uuid_and_image() {
        JsonNode body = getJson("/sso/captcha");
        JsonNode data = body.get("data");
        String uuid = data.get("uuid").asText();
        assertThat(uuid).isNotBlank();
        assertThat(data.get("img").asText()).startsWith("data:image/png;base64,");
        // 内存 Redis 替身同步记录，可直接断言存储约定
        assertThat(TEST_STATE.captchas).containsKey(uuid);
    }

    // ==================== 2. 登录失败（策略链） ====================

    @Test
    @Order(4)
    @DisplayName("登录：错误验证码 → 302 /login?error=验证码错误")
    void login_with_wrong_captcha_fails() {
        String csrf = fetchCsrf();
        String uuid = fetchCaptchaUuid();
        ExchangeResult resp = postLogin(csrf, "admin", ADMIN_PASSWORD, uuid, "XXXX");
        assertThat(loginErrorOf(resp)).contains("验证码");
    }

    @Test
    @Order(5)
    @DisplayName("登录：错误密码 → 302 /login?error=用户不存在/密码错误")
    void login_with_wrong_password_fails() {
        String csrf = fetchCsrf();
        String uuid = fetchCaptchaUuid();
        // 验证码用真实值（内存替身可直读），隔离出密码错误这一失败分支
        ExchangeResult resp = postLogin(csrf, "admin", "wrong-password", uuid, TEST_STATE.captchas.get(uuid));
        assertThat(loginErrorOf(resp)).contains("密码");
    }

    // ==================== 3. 登录成功 → 授权码 ====================

    @Test
    @Order(6)
    @DisplayName("登录：正确凭据 → 302 回跳原 /oauth2/authorize（SavedRequest 续接）")
    void login_success_resumes_authorize() {
        // 携带当前会话访问 /oauth2/authorize：未认证 → 302 /login 并保存 SavedRequest
        browserGet(authorizeUri("state-login"));

        String csrf = fetchCsrf();
        String uuid = fetchCaptchaUuid();
        ExchangeResult resp = postLogin(csrf, "admin", ADMIN_PASSWORD, uuid, TEST_STATE.captchas.get(uuid));
        String location = locationOf(resp);
        assertThat(location).contains("/oauth2/authorize");
        assertThat(location).contains("state=state-login");
        assertThat(location).contains("code_challenge=" + codeChallenge);
    }

    @Test
    @Order(7)
    @DisplayName("SSO 免登：带 AS 会话 /oauth2/authorize 直接 302 回调（不经登录页）")
    void authorize_with_session_issues_code_directly() {
        ExchangeResult resp = browserGet(authorizeUri("state-sso"));
        String location = locationOf(resp);
        assertThat(location).startsWith(REDIRECT_URI);
        assertThat(location).contains("code=");
        assertThat(location).contains("state=state-sso");
        assertThat(location).doesNotContain("/login");
        // 授权码已落库（principal 为标准类型，可被 JDBC 回读）
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM oauth2_authorization", Integer.class);
        assertThat(count).isGreaterThan(0);
    }

    // ==================== 4. 换令牌（PKCE） ====================

    @Test
    @Order(8)
    @DisplayName("换令牌：code + code_verifier → access/id token（公共客户端；7.1 公共客户端不签发 refresh_token）")
    void token_endpoint_exchanges_code_with_pkce() {
        String code = extractCode("state-token");

        JsonNode body = tokenRequest(form(
                "grant_type", "authorization_code",
                "code", code,
                "redirect_uri", REDIRECT_URI,
                "client_id", CLIENT_ID,
                "code_verifier", codeVerifier));
        assertThat(body.has("error")).as("令牌响应不应包含 error: " + body).isFalse();
        this.accessToken = body.get("access_token").asText();
        assertThat(this.accessToken).isNotBlank();
        assertThat(body.get("id_token").asText()).isNotBlank();
        assertThat(body.get("token_type").asText()).isEqualTo("Bearer");
        // [SAS 7.1 行为] OAuth2RefreshTokenGenerator 对
        // “公共客户端 + authorization_code 授权类型”不签发 refresh_token
        // （OAuth 2.1 / RFC 9700 方向：公共客户端不持有长期刷新令牌）。
        // SPA 令牌过期改走 prompt=none 静默再授权（见流程计划“令牌续期”节）。
        assertThat(body.has("refresh_token"))
                .as("7.1 公共客户端授权码流程不签发 refresh_token: " + body).isFalse();
    }

    @Test
    @Order(9)
    @DisplayName("换令牌：错误 code_verifier 被拒绝（PKCE 防重放）")
    void token_endpoint_rejects_wrong_verifier() {
        String code = extractCode("state-badverifier");
        JsonNode body = tokenRequest(form(
                "grant_type", "authorization_code",
                "code", code,
                "redirect_uri", REDIRECT_URI,
                "client_id", CLIENT_ID,
                "code_verifier", randomPkceVerifier()));
        assertThat(body.path("error").asText()).as("实际响应: " + body).isEqualTo("invalid_grant");
    }

    @Test
    @Order(10)
    @DisplayName("令牌契约：JWKS 验签 RS256 + iss/sub=user_id/username/tenant_id claims")
    void access_token_verifiable_via_jwks_with_contract_claims() throws Exception {
        JsonNode jwksJson = getJson("/oauth2/jwks");
        JWKSet jwkSet = JWKSet.parse(jwksJson.toString());
        SignedJWT parsed = SignedJWT.parse(this.accessToken);
        JWK jwk = jwkSet.getKeyByKeyId(parsed.getHeader().getKeyID());
        assertThat(jwk).as("JWKS 应包含令牌 kid").isNotNull();
        JWSVerifier verifier = new RSASSAVerifier(jwk.toRSAKey().toRSAPublicKey());
        assertThat(parsed.verify(verifier)).as("RS256 签名（AS JWK）").isTrue();
        assertThat(parsed.getJWTClaimsSet().getIssuer()).isEqualTo(ISSUER);
        assertThat(parsed.getJWTClaimsSet().getSubject()).isEqualTo(String.valueOf(ADMIN_USER_ID));
        assertThat(parsed.getJWTClaimsSet().getStringClaim("username")).isEqualTo("admin");
        assertThat(parsed.getJWTClaimsSet().getLongClaim("user_id")).isEqualTo(ADMIN_USER_ID);
        assertThat(parsed.getJWTClaimsSet().getLongClaim("tenant_id")).isEqualTo(1L);
        assertThat(parsed.getJWTClaimsSet().getAudience()).contains(CLIENT_ID);
    }

    // ==================== 5. 单点登出（SLO） ====================

    @Test
    @Order(11)
    @DisplayName("登出：/logout → 302 /login?logout=1，AS 会话失效")
    void logout_invalidates_session() {
        ExchangeResult resp = browserGet("/logout");
        assertThat(locationOf(resp)).contains("/login?logout=1");

        // 会话已失效：再次 /oauth2/authorize 回到登录页
        ExchangeResult again = browserGet(authorizeUri("state-after-logout"));
        assertThat(locationOf(again)).contains("/login");
    }

    @Test
    @Order(12)
    @DisplayName("登出撤销：SSO 登出后该用户全部授权被撤销（SLO BCL 吊销）")
    void authorizations_revoked_after_logout() {
        // [SAS 7.1] 公共客户端授权码流程不签发 refresh_token，
        // SLO 撤销面改为：LogoutSuccessEvent → JdbcLrcoreLogoutTokenRevoker
        // 按 principal_name 删除全部 oauth2_authorization 记录
        // （access/refresh/授权码一并失效）+ BCL logout_token 通知 RP 清本地态。
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM oauth2_authorization WHERE principal_name = ?",
                Integer.class, "admin");
        assertThat(count).as("SSO 登出后该用户的全部授权记录应被撤销").isZero();
    }

    @Test
    @Order(13)
    @DisplayName("子门户：直接登录成功 → 302 进入子系统展示门户（不进某一系统主页）")
    void direct_login_redirects_to_portal() {
        // 不携带 SavedRequest（未先访问 /oauth2/authorize），纯登录页登录后应进入门户
        cookieJar.clear();
        String csrf = fetchCsrf();
        String uuid = fetchCaptchaUuid();
        ExchangeResult resp = postLogin(csrf, "admin", ADMIN_PASSWORD, uuid, TEST_STATE.captchas.get(uuid));
        String location = locationOf(resp);
        assertThat(location).as("直接登录成功应进入子门户，实际: " + location)
                .contains("/sso/portal.html");
        assertThat(location).doesNotContain("/oauth2/authorize");
    }

    // ==================== 请求辅助（统一 Cookie Jar 管理） ====================

    /** 浏览器式 GET（Accept: text/html，携带 cookie jar 并回存响应 Cookie）。 */
    private ExchangeResult browserGet(String uri) {
        var result = this.client.get()
                .uri(URI.create(baseUrl() + uri))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
                .headers(h -> cookieJar.apply(h))
                .exchange().returnResult();
        cookieJar.store(result.getResponseHeaders());
        return result;
    }

    private String browserGetBody(String uri) {
        var result = this.client.get()
                .uri(URI.create(baseUrl() + uri))
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
                .headers(h -> cookieJar.apply(h))
                .exchange().returnResult();
        cookieJar.store(result.getResponseHeaders());
        return new String(result.getResponseBodyContent(), StandardCharsets.UTF_8);
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    private String fetchCsrf() {
        return csrfTokenOf(browserGetBody("/login"));
    }

    private String fetchCaptchaUuid() {
        JsonNode body = getJson("/sso/captcha");
        return body.get("data").get("uuid").asText();
    }

    /** GET JSON 端点（String 接收后自解析，规避 WebTestClient 默认 codec 对 JsonNode 的装配差异）。 */
    private JsonNode getJson(String uri) {
        var result = this.client.get()
                .uri(uri)
                .headers(h -> cookieJar.apply(h))
                .exchange().returnResult();
        cookieJar.store(result.getResponseHeaders());
        try {
            return OBJECT_MAPPER.readTree(result.getResponseBodyContent());
        } catch (Exception ex) {
            throw new IllegalStateException("JSON 响应解析失败: "
                    + new String(result.getResponseBodyContent(), StandardCharsets.UTF_8), ex);
        }
    }

    private ExchangeResult postLogin(String csrf, String username, String password,
                                     String captchaId, String captcha) {
        var result = this.client.post()
                .uri("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
                .headers(h -> cookieJar.apply(h))
                .bodyValue(form(
                        "_csrf", csrf,
                        "username", username,
                        "password", password,
                        "captchaId", captchaId,
                        "captcha", captcha))
                .exchange().returnResult();
        cookieJar.store(result.getResponseHeaders());
        return result;
    }

    /** 以当前会话发起 authorize 并提取回调 code（会话必须处于已登录态）。 */
    private String extractCode(String state) {
        String location = locationOf(browserGet(authorizeUri(state)));
        assertThat(location).startsWith(REDIRECT_URI);
        return location.substring(location.indexOf("code=") + 5, location.indexOf("&state="));
    }

    private JsonNode tokenRequest(String formBody) {
        var result = this.client.post()
                .uri("/oauth2/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formBody)
                .exchange().returnResult();
        try {
            return OBJECT_MAPPER.readTree(result.getResponseBodyContent());
        } catch (Exception ex) {
            throw new IllegalStateException("令牌端点响应解析失败: "
                    + new String(result.getResponseBodyContent(), StandardCharsets.UTF_8), ex);
        }
    }

    private static String locationOf(ExchangeResult resp) {
        return resp.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
    }

    /** 登录失败后 Location 的 error 参数（URL 解码后）。 */
    private static String loginErrorOf(ExchangeResult resp) {
        String location = locationOf(resp);
        assertThat(location).contains("/login?error=");
        String error = location.substring(location.indexOf("error=") + 6);
        return URLDecoder.decode(error, StandardCharsets.UTF_8);
    }

    private static String csrfTokenOf(String html) {
        var matcher = java.util.regex.Pattern
                .compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(html);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String authorizeUri(String state) {
        return "/oauth2/authorize?response_type=code&client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&scope=openid%20profile"
                + "&state=" + state
                + "&code_challenge=" + codeChallenge
                + "&code_challenge_method=S256";
    }

    private static String form(String... kv) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < kv.length; i += 2) {
            if (i > 0) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(kv[i], StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(kv[i + 1], StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    // ==================== PKCE 工具 ====================

    private static String randomPkceVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256Base64Url(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    // ==================== Cookie 管理（WebTestClient 无内置 cookie jar） ====================

    /** 极简 Cookie Jar：按名称收集 Set-Cookie（含登录成功后 JSESSIONID 变更），请求时回注。 */
    static class CookieJar {
        private final Map<String, String> cookies = new LinkedHashMap<>();

        void apply(HttpHeaders headers) {
            if (!cookies.isEmpty()) {
                headers.set(HttpHeaders.COOKIE, cookies.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .reduce((a, b) -> a + "; " + b)
                        .orElse(""));
            }
        }

        void store(HttpHeaders responseHeaders) {
            for (String setCookie : responseHeaders.getOrEmpty(HttpHeaders.SET_COOKIE)) {
                String[] parts = setCookie.split("=", 2);
                if (parts.length == 2) {
                    cookies.put(parts[0].trim(), parts[1].split(";")[0].trim());
                }
            }
        }

        void clear() {
            cookies.clear();
        }
    }

    // ==================== 测试上下文 ====================

    /** 测试内共享状态：验证码值（内存 Redis 替身写入时同步记录，供断言侧读取）。 */
    static final class TEST_STATE {
        static final Map<String, String> captchas = new ConcurrentHashMap<>();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(
            exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class},
            excludeName = {
                    // 屏蔽真实 Redis 装配，测试使用内存替身（验证码值经 TEST_STATE 观测）
                    "com.lrcore.common.redis.configure.RedisConfig",
                    "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
                    "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
            })
    @Import({LrcoreSsoHostConfiguration.class, LrcoreSsoSecurityConfig.class,
            SsoLoginController.class, SsoCaptchaController.class})
    static class TestApp {

        /** H2 内存数据源（OAuth2 三表 JDBC 持久化，官方 DDL 由 spring.sql.init 初始化）。
         *  destroyMethod=""：H2 JdbcDataSource 无 close/dispose 方法，测试进程退出即释放。 */
        @Bean(destroyMethod = "")
        DataSource dataSource() {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:ssotest;DB_CLOSE_DELAY=-1");
            ds.setUser("sa");
            ds.setPassword("");
            return ds;
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        /** 内存版 RedisService：验证码值同步记录到 TEST_STATE 供断言侧读取。 */
        @Bean
        RedisService redisService() {
            return new InMemoryRedisService();
        }

        /** Feign 桩：仅 admin 用户存在（BCrypt 密码），租户/企业/部门为 1。 */
        @Bean
        RemoteUserApi remoteUserApi() {
            return (username, loginType) -> {
                if ("admin".equals(username)) {
                    LoginUserDto dto = new LoginUserDto();
                    dto.setUserId(ADMIN_USER_ID);
                    dto.setUserName("admin");
                    dto.setPassword(ADMIN_BCRYPT);
                    dto.setTenantId(1L);
                    dto.setEnterpriseId(1L);
                    dto.setDeptId(1L);
                    return ApiResult.success(dto);
                }
                return ApiResult.fail("用户不存在");
            };
        }
    }

    /** 内存版 RedisService（覆盖被测方法；验证码值记录到 TEST_STATE）。 */
    static class InMemoryRedisService extends RedisService {

        private final Map<String, Object> store = new ConcurrentHashMap<>();

        InMemoryRedisService() {
            super(null);
        }

        @Override
        public <T> void setCacheObject(final String key, final T value) {
            this.store.put(key, value);
            recordCaptcha(key, value);
        }

        @Override
        public <T> void setCacheObject(final String key, final T value, final Long timeout, final TimeUnit timeUnit) {
            this.store.put(key, value);
            recordCaptcha(key, value);
        }

        private static void recordCaptcha(String key, Object value) {
            if (key.startsWith("captcha_codes:")) {
                TEST_STATE.captchas.put(key.substring("captcha_codes:".length()), String.valueOf(value));
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getCacheObject(final String key) {
            return (T) this.store.get(key);
        }

        @Override
        public boolean deleteObject(final String key) {
            return this.store.remove(key) != null;
        }

        @Override
        public Boolean hasKey(String key) {
            return this.store.containsKey(key);
        }
    }
}
