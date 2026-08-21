package com.lrcore.auth.controller;

import com.lrcore.auth.domain.SocialAuthorizeResult;
import com.lrcore.auth.domain.SocialCallbackResult;
import com.lrcore.auth.form.SocialBindForm;
import com.lrcore.auth.service.SocialLoginService;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.security.token.model.TokenDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 第三方（微信扫码）登录控制器。
 * <p>
 * 路由（均在网关放行前缀 /lrcore-auth/api/v1/auth/** 之下，匿名可访问）：
 * <ul>
 *   <li>GET  /api/v1/auth/social/authorize?platform=wechat —— 获取授权页 URL + state（前端 iframe 渲染二维码）；</li>
 *   <li>GET  /api/v1/auth/social/callback?platform=wechat&amp;code=...&amp;state=... —— 扫码回调，已绑定出令牌/未绑定回传 pending；</li>
 *   <li>POST /api/v1/auth/social/bind —— 未绑定时提交本地账号凭据完成绑定并登录。</li>
 * </ul>
 * @ClassName: SocialAuthController
 * @Author: Qi Liu
 * @Date: 2026/8/20
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "lrcore.auth.social", name = "enabled", havingValue = "true")
@Schema(description = "第三方（微信扫码）登录控制器")
@RequestMapping("/api/v1/auth/social")
public class SocialAuthController extends BaseController {

    private final SocialLoginService socialLoginService;

    @Schema(description = "发起第三方授权（获取二维码 URL）")
    @GetMapping("/authorize")
    public ApiResult<SocialAuthorizeResult> authorize(
            @Parameter(description = "平台编码（wechat/qq/github/gitee）", required = true)
            @RequestParam String platform) {
        return ApiResult.success(socialLoginService.authorize(platform));
    }

    @Schema(description = "扫码回调（已绑定出令牌 / 未绑定回传 pending）")
    @GetMapping("/callback")
    public ApiResult<SocialCallbackResult> callback(
            @Parameter(description = "平台编码", required = true) @RequestParam String platform,
            @Parameter(description = "平台授权码", required = true) @RequestParam String code,
            @Parameter(description = "防 CSRF 随机串", required = true) @RequestParam String state) {
        return ApiResult.success(socialLoginService.callback(platform, code, state));
    }

    @Schema(description = "绑定本地账号并登录")
    @PostMapping("/bind")
    public ApiResult<TokenDto> bind(@Valid @RequestBody SocialBindForm form) {
        TokenDto tokenDto = socialLoginService.bind(
                form.getPendingToken(), form.getPlatform(), form.getUsername(), form.getPassword());
        return ApiResult.success("绑定成功", tokenDto);
    }
}
