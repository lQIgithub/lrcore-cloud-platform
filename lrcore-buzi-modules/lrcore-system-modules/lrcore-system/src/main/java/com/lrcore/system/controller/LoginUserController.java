package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.login.LoginUser;
import com.lrcore.system.service.ISysUserService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 登录用户权限控制器
 * @ClassName: LoginUserController
 * @Author: Qi Liu
 * @Date: 2026/7/13 17:20
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/v1/userperm")
@RequiredArgsConstructor
@Schema(description = "登录用户权限控制器")
public class LoginUserController extends BaseController {

    private final ISysUserService sysUserService;

    /**
     * <p>方法说明</p>
     *
     * @Describe: 获取当前登录用户权限，角色，菜单等信息
     * @Param:
     * @Author: Qi Liu
     * @Date: 2026/7/13 17:21
     * @Return
     * @Version: 1.0
     */
    @GetMapping("/getInfo")
    @Schema(description = "获取当前登录用户权限，角色，菜单等信息")
    public ApiResult<LoginUser> getInfo() {
        return ApiResult.success(sysUserService.getInfo());
    }

}
