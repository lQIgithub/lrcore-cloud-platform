package com.lrcore.gateway.config;

import com.lrcore.gateway.config.properties.IgnoreWhiteProperties;
import com.lrcore.gateway.config.properties.Oauth2ServerProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.reactivestreams.Publisher;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 网关 JWT 解码器（{@link GatewaySecurityConfig#lrcoreReactiveJwtDecoder()}）回归测试 —— 纯 SAS 单轨。
 * <p>
 * 说明：若依遗留的 HS512 双 Token 轨道已全面移除，网关解码器统一为授权服务器（SAS）RS256 JWT
 * （JWKS 公钥验签 + iss/exp 校验）。本测试锁定以下行为：
 * <ol>
 *   <li>{@code decode(token)} 调用本身不得同步抛异常（必须是响应式错误，可供 onErrorResume 处理）；</li>
 *   <li>令牌无法经 SAS 解码（issuer 不可达导致 JWKS 获取失败 / 签名不符 / 过期）时，
 *       订阅阶段最终抛出异步 JwtException，网关据此 401。</li>
 * </ol>
 *
 * @ClassName: DualTrackJwtDecoderTest
 * @Version: 2.0
 */
class DualTrackJwtDecoderTest {

    /** 指向本机不可达端口的 issuer：SAS 轨道必然在 JWKS 获取阶段失败（连接拒绝） */
    private static final String UNREACHABLE_ISSUER = "http://127.0.0.1:9";

    private GatewaySecurityConfig config() {
        Oauth2ServerProperties oauth2 = new Oauth2ServerProperties();
        oauth2.setIssuer(UNREACHABLE_ISSUER);
        return new GatewaySecurityConfig(new IgnoreWhiteProperties(), oauth2);
    }

    /** 构造结构合法的 RS256 JWT（alg=RS256 头 + 标准 claims + 真实 RSA 签名） */
    private static String rs256Token() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair kp = gen.generateKeyPair();
        RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();
        Instant now = Instant.now();

        String header = b64u("{\"alg\":\"RS256\",\"typ\":\"JWT\",\"kid\":\"test-key-1\"}");
        String payload = b64u("{\"iss\":\"" + UNREACHABLE_ISSUER + "\",\"sub\":\"123\","
                + "\"iat\":" + now.getEpochSecond() + ",\"exp\":" + (now.getEpochSecond() + 1800)
                + ",\"user_id\":123,\"username\":\"tester\"}");
        String signingInput = header + "." + payload;

        java.security.Signature sig = java.security.Signature.getInstance("SHA256withRSA");
        sig.initSign(priv);
        sig.update(signingInput.getBytes(StandardCharsets.UTF_8));
        return signingInput + "." + b64u(sig.sign());
    }

    private static String b64u(Object jsonOrBytes) {
        byte[] bytes = (jsonOrBytes instanceof byte[] b) ? b : jsonOrBytes.toString().getBytes(StandardCharsets.UTF_8);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Test
    void decode_mustNotThrowSynchronously() throws Exception {
        ReactiveJwtDecoder decoder = config().lrcoreReactiveJwtDecoder();
        String token = rs256Token();
        Publisher<org.springframework.security.oauth2.jwt.Jwt> publisher =
                assertDoesNotThrow(() -> decoder.decode(token),
                        "decode() 必须以响应式方式失败，不得同步抛异常（否则 onErrorResume 无法捕获）");
        assertNotNull(publisher);
    }

    @Test
    void invalidOrUnreachableIssuerTokenFailsAsynchronously() throws Exception {
        ReactiveJwtDecoder decoder = config().lrcoreReactiveJwtDecoder();
        String token = rs256Token();

        Throwable failure = assertThrows(Throwable.class,
                () -> decoder.decode(token)
                        .block(java.time.Duration.ofSeconds(10)),
                "不可达 issuer 下 SAS 解码必然失败（JWKS 获取连接拒绝）");

        String chain = failureChain(failure);
        // 网关解码器是纯 SAS 实现，不应出现任何若依遗留 HS512 相关错误文案
        assertTrue(!chain.contains("旧格式令牌校验失败") && !chain.contains("HS512"),
                "仍存在若依遗留的双 Token 校验逻辑: " + chain);
    }

    /** 展开异常链为可读字符串 */
    private static String failureChain(Throwable t) {
        StringBuilder sb = new StringBuilder();
        while (t != null) {
            sb.append(t.getClass().getSimpleName()).append(": ").append(t.getMessage()).append(" | ");
            t = t.getCause();
        }
        return sb.toString();
    }
}