package com.lrcore.auth.controller;

import com.lrcore.auth.form.LoginForm;
import com.lrcore.auth.form.RegisterBody;
import com.lrcore.auth.form.UnLockBody;
import com.lrcore.auth.service.SysLoginService;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.utils.JwtUtils;
import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.security.auth.AuthUtil;
import com.lrcore.common.security.token.model.TokenDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 令牌 控制器
 * @ClassName: TokenController
 * @Author: Qi Liu
 * @Date: 2026/4/2 19:28
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Schema(description = "令牌控制器")
@RequestMapping("/api/v1/auth")
public class TokenController extends BaseController {
    private final SysLoginService sysLoginService;

    @Schema(description = "生成密码")
    @PostMapping("/genPassword")
    public ApiResult<String> genPassword(@RequestBody String password) {
        return ApiResult.success(SecurityUtils.encryptPassword(password));
    }

    @Schema(description = "用户登录")
    @PostMapping("/login")
    public ApiResult<TokenDto> login(@RequestBody LoginForm loginForm) {
//        String password = loginForm.getPassword();
//        String encryptPassword = SecurityUtils.encryptPassword(password);
//        log.info("password>>>>>>>密码:{}", encryptPassword);
        return sysLoginService.login(loginForm);
    }

    @Schema(description = "刷新令牌")
    @PostMapping("/refreshToken")
    public ApiResult<TokenDto> refreshToken(@Parameter(description = "刷新令牌") @RequestParam String refreshToken) {
        return sysLoginService.refreshToken(refreshToken);
    }

    @Schema(description = "用户登出")
    @DeleteMapping("/logout")
    public ApiResult<?> logout(HttpServletRequest request) {
        String token = SecurityUtils.getToken(request);
        if (FunStrUtils.isNotEmpty(token)) {
            String username = JwtUtils.getUserName(token);
            AuthUtil.logoutByToken(token);
            sysLoginService.logout(username);
        }
        return ApiResult.success();
    }

    @Schema(description = "用户注册")
    @PostMapping("/register")
    public ApiResult<?> register(@RequestBody RegisterBody registerBody) {
        //sysLoginService.register(registerBody.getUsername(), registerBody.getPassword());
        return ApiResult.success();
    }

    /**
     * 解锁屏幕
     */
    @Schema(description = "解锁屏幕")
    @PostMapping("/unlockscreen")
    public ApiResult<?> unlockScreen(@RequestBody UnLockBody unLockBody) {
        sysLoginService.unlock(unLockBody.getPassword());
        return ApiResult.success("解锁成功");
    }
}
