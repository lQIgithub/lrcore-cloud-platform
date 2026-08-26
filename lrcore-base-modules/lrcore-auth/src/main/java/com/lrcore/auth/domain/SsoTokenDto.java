package com.lrcore.auth.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSO/SAS 标准 OAuth2 访问令牌响应结构（取代若依 HS512 双 Token 的 TokenDto）。
 *            <ul>
 *              <li>access_token：SAS 授权服务器签发的 RS256 JWT（JWKS 公钥验签）；</li>
 *              <li>expires_in：访问令牌有效秒数（秒）；</li>
 *              <li>scope：已授权范围（如 openid profile）；</li>
 *              <li>id_token（可选）：OIDC 身份令牌，用于前端通道登出 id_token_hint。</li>
 *            </ul>
 * @ClassName: SsoTokenDto
 * @Author: lrcore
 * @Date 2026/8/24
 * @Version 1.0
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "SSO OAuth2 访问令牌响应")
public class SsoTokenDto {

    @Schema(description = "访问令牌（SAS RS256 JWT）")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(description = "令牌类型（恒为 Bearer）")
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "访问令牌有效秒数")
    @JsonProperty("expires_in")
    private Long expiresIn;

    @Schema(description = "已授权范围")
    private String scope;

    @Schema(description = "OIDC 身份令牌（可为空）")
    @JsonProperty("id_token")
    private String idToken;
}