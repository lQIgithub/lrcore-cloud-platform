package com.lrcore.auth.social;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lrcore.auth.controller.SocialAuthController;
import com.lrcore.auth.handler.LrcoreAuthExceptionHandler;
import com.lrcore.auth.service.SasAccessTokenIssuer;
import com.lrcore.auth.service.SocialLoginService;
import com.lrcore.common.auth.key.LrcoreJwkSourceFactory;
import com.lrcore.common.auth.social.SocialAccountBinding;
import com.lrcore.common.auth.social.SocialAccountBindingRepository;
import com.lrcore.common.auth.social.SocialAccountBindingService;
import com.lrcore.common.auth.social.SocialPlatform;
import com.lrcore.common.auth.token.LrcoreTokenCustomizer;
import com.lrcore.common.auth.user.LrcoreUser;
import com.lrcore.common.auth.user.LrcoreUserDetailsService;
import com.lrcore.common.auth.user.LrcoreUserSource;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.login.LoginUserDto;
import com.lrcore.common.redis.service.RedisService;
import com.lrcore.system.api.RemoteUserApi;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <p>类模块说明</p>
 *
 * @Describe: QQ 互联登录全链路集成测试（真实 HTTP 打到真实 Tomcat，服务内部链路全真）。
 * <p>
 * 真实部件：SocialAuthController / SocialLoginService / SasAccessTokenIssuer
 *          （SAS RS256 JWT 签发，取代若依 TokenService 的 HS512 双 Token）/
 *          LrcoreTokenCustomizer（claims 契约）/ LrcoreAuthExceptionHandler /
 *          LrcoreSocialAuthConfiguration 自动装配的 QqSocialPlatformClient
 *          （api-base-url 指向本地 Mock QQ）/ SocialAccountBindingService。
 * 替身部件：Redis（内存 Map）/ 绑定仓库（内存）/ RemoteUserApi（Feign 桩）。
 * <p>
 * QQ 特有链路（与微信扫码的差异）：令牌响应不含 openid → 客户端先调 /oauth2.0/me 查询 openid
 * → 再调 /user/get_user_info（扁平 JSON + oauth_consumer_key）拉取资料。
 * 覆盖：授权 URL 生成（get_user_info scope）、state 一次性消费、已绑定出令牌、
 *      未绑定 pending（openId/昵称/头像）、绑定成功（凭据校验+落库+出令牌）、
 *      绑定失败（错误密码不落库）、QQ 侧无效授权码、未启用平台被拒绝。
 * @ClassName: SocialLoginFlowQqTest
 * @Author: Qi Liu
 * @Date: 2026/8/21
 * @Version: 1.0
 */
@SpringBootTest(
        classes = SocialLoginFlowQqTest.TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.main.banner-mode=off",
                "logging.level.root=WARN",
                "lrcore.auth.enabled=true",
                "lrcore.auth.social.enabled=true",
                "lrcore.auth.social.qq.client-id=qq-test-appid",
                "lrcore.auth.social.qq.client-secret=qq-test-secret",
                "lrcore.auth.social.qq.redirect-uri=http://localhost:3000/#/sso/callback",
                "encrypt.aes.enabled=false",
                "jasypt.encryptor.password=test-jasypt-password",
                "truelicense.enabled=false",
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.service-registry.auto-registration.enabled=false"
        })
class SocialLoginFlowQqTest {

    static final String ADMIN_PASSWORD = "Lr@123456.";
    static final String ADMIN_BCRYPT = new BCryptPasswordEncoder().encode(ADMIN_PASSWORD);
    static final long ADMIN_USER_ID = 7264590000000000071L;

    /** 与线上 MVC 相同的 Jackson 2 + JavaTime 模块（ApiResult.serviceDateTime 反序列化需要）。 */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().findAndRegisterModules();

    private static final TypeReference<ApiResult<JsonNode>> TR_DATA = new TypeReference<>() {
    };

    private static final MockQqServer QQ = startMockQq();

    private static MockQqServer startMockQq() {
        try {
            return new MockQqServer();
        } catch (IOException ex) {
            throw new IllegalStateException("启动 Mock QQ 服务失败", ex);
        }
    }

    @DynamicPropertySource
    static void qqApiBase(DynamicPropertyRegistry registry) {
        // 仅 API 基地址指向 Mock（令牌 / me / 用户信息），授权页仍走官方地址
        registry.add("lrcore.auth.social.qq.api-base-url",
                () -> "http://127.0.0.1:" + QQ.port());
    }

    @LocalServerPort
    private int port;

    @Autowired
    private SocialAccountBindingRepository bindingRepository;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        this.client = WebTestClient.bindToServer()
                .baseUrl("http://127.0.0.1:" + port)
                .responseTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ==================== 1. 授权 URL ====================

    @Test
    @DisplayName("授权：返回 QQ 授权页 URL（graph.qq.com + get_user_info scope + state）")
    void authorize_returns_qq_authorize_url_and_state() {
        String body = this.client.get()
                .uri("/api/v1/auth/social/authorize?platform=qq")
                .exchange().expectBody(String.class).returnResult().getResponseBody();
        ApiResult<JsonNode> result = parse(body);
        JsonNode data = result.getData();

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.isSuccess()).isTrue();
        assertThat(data.get("platform").asText()).isEqualTo("qq");
        assertThat(data.get("state").asText()).isNotBlank();
        assertThat(data.get("authorizeUrl").asText())
                .startsWith("https://graph.qq.com/oauth2.0/authorize?")
                .contains("response_type=code")
                .contains("client_id=qq-test-appid")
                .contains("scope=get_user_info")
                .contains("state=" + data.get("state").asText());
    }

    // ==================== 2. 回调：已绑定 → 出令牌 ====================

    @Test
    @DisplayName("回调：已绑定用户 → 平台令牌（SAS RS256 JWT）")
    void callback_bound_user_gets_platform_token() {
        bindingRepository.save(new SocialAccountBinding(
                null, ADMIN_USER_ID, "admin", SocialPlatform.QQ,
                "oid-admin-bound", "QQ管理员", "https://qq.example.com/admin.png"));
        QQ.registerCode("code-bound-1", "oid-admin-bound");

        ApiResult<JsonNode> result = callback("code-bound-1", authorize());

        assertThat(result.getCode()).isEqualTo("200");
        JsonNode data = result.getData();
        assertThat(data.get("bound").asBoolean()).isTrue();
        JsonNode token = data.get("token");
        assertThat(token.isNull()).isFalse();
        assertSasJwt(token.get("access_token").asText()); // SAS RS256 JWT
        assertThat(token.get("token_type").asText()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("回调：state 一次性 —— 重放同一 state 被拒绝")
    void callback_state_is_single_use() {
        bindingRepository.save(new SocialAccountBinding(
                null, ADMIN_USER_ID, "admin", SocialPlatform.QQ, "oid-admin-replay", null, null));
        QQ.registerCode("code-replay-1", "oid-admin-replay");
        QQ.registerCode("code-replay-2", "oid-admin-replay");

        String state = authorize();
        assertThat(callback("code-replay-1", state).getCode()).isEqualTo("200");

        ApiResult<JsonNode> replay = callback("code-replay-2", state);
        assertThat(replay.getCode()).isEqualTo("500");
        assertThat(replay.getMessage()).contains("重新扫码");
    }

    @Test
    @DisplayName("回调：未启用平台（wechat 未配置）被拒绝")
    void callback_disabled_platform_rejected() {
        ApiResult<JsonNode> result = rawCallback("wechat", "x", authorize());
        assertThat(result.getCode()).isEqualTo("500");
    }

    @Test
    @DisplayName("回调：QQ 侧无效授权码 → 业务失败（不泄露平台细节）")
    void callback_invalid_qq_code_fails() {
        ApiResult<JsonNode> result = callback("code-not-registered", authorize());
        assertThat(result.getCode()).isEqualTo("500");
        assertThat(result.getMessage()).contains("第三方登录失败");
    }

    // ==================== 3. 回调：未绑定 → pending ====================

    @Test
    @DisplayName("回调：未绑定用户 → bound=false + pendingToken + openId/昵称/头像")
    void callback_unbound_user_gets_pending() {
        QQ.registerCode("code-new-1", "oid-first-time");

        ApiResult<JsonNode> result = callback("code-new-1", authorize());

        assertThat(result.getCode()).isEqualTo("200");
        JsonNode data = result.getData();
        assertThat(data.get("bound").asBoolean()).isFalse();
        assertThat(data.get("token").isNull()).isTrue();
        assertThat(data.get("pendingToken").asText()).isNotBlank();
        assertThat(data.get("openId").asText()).isEqualTo("oid-first-time");
        assertThat(data.get("nickname").asText()).isEqualTo("QQ用户-oid-first-time");
        // 头像取 figureurl_qq_2（Mock 返回 /2.png）
        assertThat(data.get("avatarUrl").asText()).contains("oid-first-time");
        assertThat(data.get("avatarUrl").asText()).contains("/2.png");
    }

    // ==================== 4. 绑定 ====================

    @Test
    @DisplayName("绑定：正确凭据 → 绑定落库 + 直接登录出令牌；再次授权即已绑定")
    void bind_with_correct_password_binds_and_logs_in() {
        QQ.registerCode("code-bind-1", "oid-bind-target");
        JsonNode unbound = callback("code-bind-1", authorize()).getData();
        assertThat(unbound.get("bound").asBoolean()).isFalse();

        // 正确密码绑定并登录
        ApiResult<JsonNode> bindResult = bind(unbound.get("pendingToken").asText(), ADMIN_PASSWORD);
        assertThat(bindResult.getCode()).isEqualTo("200");
        assertSasJwt(bindResult.getData().get("access_token").asText());

        // 绑定已落库（platform 为 qq）
        SocialAccountBinding binding =
                bindingRepository.findByPlatformAndOpenId(SocialPlatform.QQ, "oid-bind-target");
        assertThat(binding).isNotNull();
        assertThat(binding.userId()).isEqualTo(ADMIN_USER_ID);
        assertThat(binding.username()).isEqualTo("admin");
        assertThat(binding.platform()).isEqualTo(SocialPlatform.QQ);

        // 再次授权（同一 openid）→ 已绑定直接出令牌
        QQ.registerCode("code-bind-2", "oid-bind-target");
        ApiResult<JsonNode> second = callback("code-bind-2", authorize());
        assertThat(second.getCode()).isEqualTo("200");
        assertThat(second.getData().get("bound").asBoolean()).isTrue();
    }

    @Test
    @DisplayName("绑定：错误密码 → 拒绝且不落库")
    void bind_with_wrong_password_rejected_no_binding() {
        QQ.registerCode("code-bind-3", "oid-wrong-pw");
        JsonNode unbound = callback("code-bind-3", authorize()).getData();
        assertThat(unbound.get("bound").asBoolean()).isFalse();

        ApiResult<JsonNode> bindResult = bind(unbound.get("pendingToken").asText(), "wrong-password");
        assertThat(bindResult.getCode()).isEqualTo("500");
        assertThat(bindingRepository.findByPlatformAndOpenId(SocialPlatform.QQ, "oid-wrong-pw")).isNull();
    }

    @Test
    @DisplayName("绑定：pending 一次性 —— 重复提交被拒绝")
    void bind_pending_is_single_use() {
        QQ.registerCode("code-bind-4", "oid-single-use");
        JsonNode unbound = callback("code-bind-4", authorize()).getData();

        ApiResult<JsonNode> first = bind(unbound.get("pendingToken").asText(), ADMIN_PASSWORD);
        assertThat(first.getCode()).isEqualTo("200");

        ApiResult<JsonNode> second = bind(unbound.get("pendingToken").asText(), ADMIN_PASSWORD);
        assertThat(second.getCode()).isEqualTo("500");
        assertThat(second.getMessage()).contains("重新扫码");
    }

    // ==================== 测试辅助 ====================

    /** 发起 QQ 授权，返回 state。 */
    private String authorize() {
        String body = this.client.get()
                .uri("/api/v1/auth/social/authorize?platform=qq")
                .exchange().expectBody(String.class).returnResult().getResponseBody();
        ApiResult<JsonNode> result = parse(body);
        assertThat(result.getCode()).isEqualTo("200");
        return result.getData().get("state").asText();
    }

    /** 执行 QQ 授权回调。 */
    private ApiResult<JsonNode> callback(String code, String state) {
        return rawCallback("qq", code, state);
    }

    /** 执行授权回调（platform 参数可变，用于未启用平台场景）。 */
    private ApiResult<JsonNode> rawCallback(String platform, String code, String state) {
        String body = this.client.get()
                .uri("/api/v1/auth/social/callback?platform={p}&code={c}&state={s}", platform, code, state)
                .exchange().expectBody(String.class).returnResult().getResponseBody();
        return parse(body);
    }

    /** 提交绑定表单。 */
    private ApiResult<JsonNode> bind(String pendingToken, String password) {
        String body = this.client.post()
                .uri("/api/v1/auth/social/bind")
                .bodyValue(Map.of(
                        "pendingToken", pendingToken,
                        "platform", "qq",
                        "username", "admin",
                        "password", password))
                .exchange().expectBody(String.class).returnResult().getResponseBody();
        return parse(body);
    }

    /** 按线上 Jackson 2 契约解析响应体（与前端 axios 收到的一致）。 */
    private static ApiResult<JsonNode> parse(String body) {
        try {
            return OBJECT_MAPPER.readValue(body, TR_DATA);
        } catch (IOException ex) {
            throw new IllegalStateException("响应体解析失败: " + body, ex);
        }
    }

    /**
     * 断言 SAS 签发的访问令牌：标准 JWT 三段式，且 header alg 为 RS256。
     */
    private static void assertSasJwt(String jwt) {
        assertThat(jwt).isNotBlank();
        assertThat(jwt.split("\\.")).as("SAS JWT 应有三段").hasSize(3);
        try {
            SignedJWT parsed = SignedJWT.parse(jwt);
            assertThat(parsed.getHeader().getAlgorithm())
                    .as("SAS 签发令牌应使用 RS256")
                    .isEqualTo(com.nimbusds.jose.JWSAlgorithm.RS256);
        } catch (java.text.ParseException ex) {
            throw new AssertionError("非合法 JWT: " + jwt, ex);
        }
    }

    // ==================== 测试上下文 ====================

    @SpringBootConfiguration
    @EnableAutoConfiguration(
            exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class},
            excludeName = {
                    // 屏蔽认证中心真实安全链（SAS/资源服务器链），测试链统一放行
                    "com.lrcore.common.auth.config.AuthorizationSecurityConfig",
                    // 屏蔽真实 Redis 装配，测试使用内存替身
                    "com.lrcore.common.redis.configure.RedisConfig",
                    "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
                    "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
            })
    @Import({SocialLoginService.class, SocialAuthController.class, SasAccessTokenIssuer.class,
            LrcoreAuthExceptionHandler.class})
    static class TestApp {

        /**
         * Bean 名必须为 resourceServerFilterChain：common-security 的
         * LrcoreResourceServerSecurityConfiguration 以此为让位条件，测试链独占安全链。
         */
        @Bean
        SecurityFilterChain resourceServerFilterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }

        @Bean
        RedisService redisService() {
            return new InMemoryRedisService();
        }

        @Bean
        SocialAccountBindingRepository socialAccountBindingRepository() {
            return new InMemoryBindingRepository();
        }

        /** Feign 桩：仅认证中心用到的 admin 用户存在，密码为 ADMIN_BCRYPT。 */
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

        /** 平台统一密码编码器（BCrypt；lrcore-common-core 的 PasswordEncoderAutoConfiguration 会因缺失让位）。 */
        @Bean
        PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        /**
         * 用户数据源 Bridge（与 main 的 LrcoreSsoHostConfiguration.lrcoreUserSource 同构）：
         * RemoteUserApi.getUserInfo(username, "web") → LrcoreUser；不存在返回 null。
         */
        @Bean
        LrcoreUserSource lrcoreUserSource(RemoteUserApi remoteUserApi) {
            return username -> {
                ApiResult<LoginUserDto> result = remoteUserApi.getUserInfo(username, "web");
                if (result == null || !result.isSuccess() || result.getData() == null) {
                    return null;
                }
                LoginUserDto dto = result.getData();
                return new LrcoreUser(dto.getUserId(), dto.getUserName(), dto.getPassword(),
                        Collections.emptyList(), null,
                        dto.getTenantId(), dto.getEnterpriseId(), dto.getDeptId());
            };
        }

        /** 统一用户详情服务（账户状态校验 + 映射 LrcoreUserDetails）。 */
        @Bean
        LrcoreUserDetailsService lrcoreUserDetailsService(LrcoreUserSource lrcoreUserSource) {
            return new LrcoreUserDetailsService(lrcoreUserSource);
        }

        /** SAS 签名密钥（内存随机 RSA，开发/测试模式；common-auth 的 Key 装配因缺失自动让位）。 */
        @Bean
        JWKSource<SecurityContext> jwkSource() {
            return LrcoreJwkSourceFactory.inMemory("social-test-key");
        }

        @Bean
        AuthorizationServerSettings authorizationServerSettings() {
            return AuthorizationServerSettings.builder().issuer("http://localhost:10802").build();
        }

        /** 授权记录持久化（内存实现，供 SasAccessTokenIssuer 落库）。 */
        @Bean
        OAuth2AuthorizationService authorizationService() {
            return new InMemoryOAuth2AuthorizationService();
        }

        /** 内置 SPA 公共客户端（web-admin-spa）：授权码 + openid/profile，供 SAS 签发令牌。 */
        @Bean
        RegisteredClientRepository registeredClientRepository() {
            RegisteredClient spa = RegisteredClient.withId("1f0e0d0c-0a0b-4c0d-8e0f-000000000003")
                    .clientId(SasAccessTokenIssuer.SSO_CLIENT_ID)
                    .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("http://localhost:3000/sso/oauth-callback")
                    .scope(OidcScopes.OPENID)
                    .scope(OidcScopes.PROFILE)
                    .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                    .tokenSettings(TokenSettings.builder()
                            .accessTokenTimeToLive(Duration.ofMinutes(30))
                            .build())
                    .build();
            return new InMemoryRegisteredClientRepository(spa);
        }

        /** SAS 令牌业务 claims 自定义器（sub/user_id/username/roles/tenant_id/ent_id/dept_id）。 */
        @Bean
        LrcoreTokenCustomizer lrcoreTokenCustomizer() {
            return new LrcoreTokenCustomizer();
        }

        /** 绑定服务（构造器仅依赖绑定仓库；common-auth 自动装配因 @ConditionalOnMissingBean 让位）。 */
        @Bean
        SocialAccountBindingService socialAccountBindingService(
                SocialAccountBindingRepository socialAccountBindingRepository) {
            return new SocialAccountBindingService(socialAccountBindingRepository);
        }
    }

    /** 内存版 RedisService（覆盖全部被测方法，不触网）。 */
    static class InMemoryRedisService extends RedisService {

        private final Map<String, Object> store = new ConcurrentHashMap<>();

        InMemoryRedisService() {
            super(null);
        }

        @Override
        public <T> void setCacheObject(final String key, final T value) {
            this.store.put(key, value);
        }

        @Override
        public <T> void setCacheObject(final String key, final T value, final Long timeout, final TimeUnit timeUnit) {
            this.store.put(key, value);
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
