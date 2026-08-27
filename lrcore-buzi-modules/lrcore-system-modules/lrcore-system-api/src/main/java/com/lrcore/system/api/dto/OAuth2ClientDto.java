package com.lrcore.system.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Set;

/**
 * <p>类的说明</p>
 *
 * @Describe: OAuth2 客户端展示 DTO —— 后台管理「客户端管理」页使用，字段裁剪至展示所需，
 *            不暴露客户端密钥等敏感信息。
 * @ClassName: OAuth2ClientDto
 * @Author: lrcore
 * @Date 2026/8/27
 * @Version 1.0
 */
@Data
@Schema(description = "OAuth2 客户端展示 DTO")
public class OAuth2ClientDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "客户端 clientId")
    private String clientId;

    @Schema(description = "客户端名称")
    private String clientName;

    @Schema(description = "授权回调地址列表")
    private Set<String> redirectUris;

    @Schema(description = "登出回跳地址列表")
    private Set<String> postLogoutRedirectUris;

    @Schema(description = "是否公共 PKCE 客户端")
    private Boolean publicClient;

    @Schema(description = "授权作用域")
    private Set<String> scopes;

    @Schema(description = "是否禁用（管理标记）")
    private Boolean disabled;
}