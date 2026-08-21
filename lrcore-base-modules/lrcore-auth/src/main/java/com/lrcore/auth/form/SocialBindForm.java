package com.lrcore.auth.form;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 第三方（微信）账号绑定表单 —— 首次扫码登录（未绑定）时，
 *            前端展示本地账号绑定表单，提交本表单完成「本地账号 ↔ 平台身份」绑定并直接登录。
 * @ClassName: SocialBindForm
 * @Author: Qi Liu
 * @Date: 2026/8/20
 * @Version: 1.0
 */
@Data
@Schema(description = "第三方账号绑定表单")
public class SocialBindForm {

    @NotBlank(message = "绑定凭据不能为空")
    @Schema(description = "回调返回的短时绑定凭据（pendingToken）")
    private String pendingToken;

    @NotBlank(message = "平台不能为空")
    @Schema(description = "平台编码（wechat/qq/github/gitee）")
    private String platform;

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "本地账号用户名")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "本地账号密码")
    private String password;
}
