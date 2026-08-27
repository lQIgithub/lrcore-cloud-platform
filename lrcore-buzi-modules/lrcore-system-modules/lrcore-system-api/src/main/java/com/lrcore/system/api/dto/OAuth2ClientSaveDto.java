package com.lrcore.system.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * <p>类的说明</p>
 *
 * @Describe: OAuth2 客户端「注册 / 更新」传输 DTO —— 后台管理系统（lrcore-system）对外
 *            接收管理员表单，经 Feign 序列化透传给认证中心（lrcore-auth）。
 *            字段语义与 lrcore-auth 侧 {@code OAuth2ClientSaveForm} 一致。
 * @ClassName: OAuth2ClientSaveDto
 * @Author: lrcore
 * @Date 2026/8/27
 * @Version 1.0
 */
@Data
@Accessors(chain = true)
@Schema(description = "OAuth2 客户端注册/更新传输 DTO")
public class OAuth2ClientSaveDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "注册客户端时留空；更新时携带已注册的 clientId")
    private String clientId;

    @Schema(description = "客户端名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "客户端名称不能为空")
    @Size(max = 200, message = "客户端名称长度不能超过200个字符")
    private String clientName;

    @Schema(description = "客户端密钥（公共 PKCE 客户端留空；服务端形态必填）")
    @Size(max = 100, message = "客户端密钥长度不能超过100个字符")
    private String clientSecret;

    @Schema(description = "授权回调地址（可多个）", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, message = "至少配置一个授权回调地址")
    private Set<String> redirectUris;

    @Schema(description = "登出回跳地址（可多个）")
    private Set<String> postLogoutRedirectUris;

    @Schema(description = "授权作用域；缺省为 openid profile")
    private Set<String> scopes;

    @Schema(description = "是否公共 PKCE 客户端（默认 true）")
    private Boolean publicClient;

    @Schema(description = "访问令牌有效期（分钟），缺省 30")
    private Integer accessTokenTtlMinutes;

    @Schema(description = "刷新令牌有效期（天），缺省 7")
    private Integer refreshTokenTtlDays;

    @Schema(description = "是否禁用（管理标记）")
    private Boolean disabled;
}