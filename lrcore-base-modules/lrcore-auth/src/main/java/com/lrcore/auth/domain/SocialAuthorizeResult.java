package com.lrcore.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 第三方（微信扫码）授权请求结果。
 *            前端用 authorizeUrl 作为 iframe src 渲染二维码；state 用于回调时 CSRF 校验。
 * @ClassName: SocialAuthorizeResult
 * @Author: Qi Liu
 * @Date: 2026/8/20
 * @Version: 1.0
 */
@Data
@Schema(description = "第三方授权请求结果")
public class SocialAuthorizeResult {

    @Schema(description = "平台编码（wechat/qq/github/gitee）")
    private String platform;

    @Schema(description = "防 CSRF 随机串，回调时须原样带回")
    private String state;

    @Schema(description = "平台授权页完整 URL（微信扫码为 qrconnect + #wechat_redirect，可直接作为 iframe src）")
    private String authorizeUrl;
}
