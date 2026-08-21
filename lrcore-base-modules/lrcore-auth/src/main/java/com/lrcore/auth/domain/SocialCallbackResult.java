package com.lrcore.auth.domain;

import com.lrcore.common.security.token.model.TokenDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 第三方（微信扫码）回调结果。
 * <ul>
 *   <li>已绑定：{@link #bound}=true，{@link #token} 为平台令牌（与账号密码登录同构）；</li>
 *   <li>未绑定：{@link #bound}=false，{@link #pendingToken} 为短时绑定凭据，
 *       前端据此展示绑定表单，提交 POST /bind 完成绑定并登录。</li>
 * </ul>
 * @ClassName: SocialCallbackResult
 * @Author: Qi Liu
 * @Date: 2026/8/20
 * @Version: 1.0
 */
@Data
@Schema(description = "第三方回调结果")
public class SocialCallbackResult {

    @Schema(description = "是否已绑定本地账号")
    private boolean bound;

    @Schema(description = "平台编码")
    private String platform;

    @Schema(description = "已绑定时返回的平台令牌")
    private TokenDto token;

    @Schema(description = "未绑定时返回的短时绑定凭据（约 10 分钟有效，一次性）")
    private String pendingToken;

    @Schema(description = "平台侧用户唯一标识（openId）")
    private String openId;

    @Schema(description = "平台昵称")
    private String nickname;

    @Schema(description = "平台头像地址")
    private String avatarUrl;
}
