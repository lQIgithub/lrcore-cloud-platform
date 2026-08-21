package com.lrcore.gateway.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 授权服务器（lrcore-auth，Spring Authorization Server）连接配置，
 *            供网关校验 SAS 签发的 RS256 JWT 使用。
 * <p>
 * 配置示例（Nacos，网关配置 lrcore-gateway-{env}.yml）：
 * <pre>
 * lrcore:
 *   oauth2:
 *     issuer: http://lrcore.com:10802        # 必须与令牌 iss 声明完全一致
 *     jwk-set-uri:                            # 可选；缺省 = issuer + /oauth2/jwks
 *     validate-issuer: true                   # 是否校验 iss 声明
 * </pre>
 * <p>
 * 说明：issuer / jwk-set-uri 指向<b>服务间可达</b>的地址（本机直连 auth 服务端口，
 * 或经网关的对外地址，取决于部署形态）；仅在启动时用于构建解码器，
 * 变更需重启网关生效。
 *
 * @ClassName: Oauth2ServerProperties
 * @Author: lrcore
 * @Date: 2026/8/21
 * @Version: 1.0
 */
@Configuration
@RefreshScope
@ConfigurationProperties(prefix = "lrcore.oauth2")
public class Oauth2ServerProperties {

    /**
     * 授权服务器 Issuer（对外颁发地址，须与令牌 iss 声明完全一致）。
     * 留空则关闭 SAS 令牌校验（仅兼容旧 HS512 双令牌链路）。
     */
    private String issuer = "http://lrcore.com:10802";

    /**
     * JWKS 公钥地址（/oauth2/jwks）。留空时默认取 issuer + /oauth2/jwks。
     * 注意：此处应为服务间可达地址（如内网直连 auth 服务端口）。
     */
    private String jwkSetUri;

    /**
     * 是否校验令牌 iss 声明与 issuer 一致（建议开启）。
     */
    private boolean validateIssuer = true;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public boolean isValidateIssuer() {
        return validateIssuer;
    }

    public void setValidateIssuer(boolean validateIssuer) {
        this.validateIssuer = validateIssuer;
    }
}
