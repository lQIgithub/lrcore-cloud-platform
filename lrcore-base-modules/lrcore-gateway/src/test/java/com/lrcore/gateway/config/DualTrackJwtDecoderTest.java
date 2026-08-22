package com.lrcore.gateway.config;

import com.lrcore.gateway.config.properties.IgnoreWhiteProperties;
import com.lrcore.gateway.config.properties.Oauth2ServerProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;
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
 * 双轨 JWT 解码器（{@link GatewaySecurityConfig#lrcoreReactiveJwtDecoder()}）回归测试。
 * <p>
 * 锁定 2026-08-22 真实环境 E2E 发现的缺陷：旧 HS512 轨道对 RS256 令牌
 * <b>同步抛异常</b>（JJWT: "Key bytes can only be specified for HMAC signatures"，
 * {@code InvalidKeyException} 继承 {@code GeneralSecurityException} 而非 {@link JwtException}）。
 * 若旧轨道用 {@code Mono.just(buildLegacyJwt(token))} 直接组装，异常发生在 Publisher
 * 组装期（同步抛出）而非订阅期（错误信号），{@code onErrorResume} 无法捕获，
 * 回退 SAS 轨道成为死代码 —— 所有 SSO 令牌被网关整体拒绝。
 * <p>
 * 断言（hermetic，不依赖真实 AS / 网络）：
 * <ol>
 *   <li>{@code decode(rs256Token)} 调用本身不得同步抛异常（缺陷修复前的直接症状）；</li>
 *   <li>订阅后旧轨失败必须回退 SAS 轨道：最终错误来自 JWKS 获取失败（指向不可达
 *       issuer），而非旧轨包装的 "旧格式令牌校验失败"。</li>
 * </ol>
 *
 * @ClassName: DualTrackJwtDecoderTest
 * @Version: 1.0
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
    void rs256Token_decodeMustNotThrowSynchronously() throws Exception {
        // 缺陷修复前：此处同步抛出 JwtException("旧格式令牌校验失败: Key bytes can only be...")
        ReactiveJwtDecoder decoder = config().lrcoreReactiveJwtDecoder();
        String token = rs256Token();
        Publisher<org.springframework.security.oauth2.jwt.Jwt> publisher =
                assertDoesNotThrow(() -> decoder.decode(token),
                        "旧轨道同步抛异常会绕过 onErrorResume，回退 SAS 轨道成为死代码");
        assertNotNull(publisher);
    }

    @Test
    void rs256Token_legacyFailureMustFallBackToSasTrack() throws Exception {
        ReactiveJwtDecoder decoder = config().lrcoreReactiveJwtDecoder();
        String token = rs256Token();

        Throwable failure = assertThrows(Throwable.class,
                () -> decoder.decode(token)
                        .block(java.time.Duration.ofSeconds(10)),
                "不可达 issuer 下两轨都应失败（最终错误来自 SAS 轨的 JWKS 获取）");

        // 最终错误必须是 SAS 轨道的 JWKS 获取失败（连接拒绝/超时），
        // 而不是旧轨包装的 JwtException —— 后者意味着回退没有发生
        String chain = failureChain(failure);
        assertTrue(!chain.contains("旧格式令牌校验失败"),
                "回退 SAS 轨道未执行，最终错误仍停留在旧 HS512 轨道: " + chain);
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
