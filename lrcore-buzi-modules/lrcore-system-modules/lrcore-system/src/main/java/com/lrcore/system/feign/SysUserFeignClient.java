package com.lrcore.system.feign;

import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.login.LoginUserDto;
import com.lrcore.system.api.RemoteUserApi;
import com.lrcore.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 用户信息 控制器
 * @ClassName: SysUserController
 * @Author: Qi Liu
 * @Date: 2026/4/1 12:59
 * @Version: 1.0
 */
@RestController
@RequiredArgsConstructor
public class SysUserFeignClient implements RemoteUserApi {

    private final ISysUserService userService;

    @Override
    public ApiResult<LoginUserDto> getUserInfo(String username, String loginType) {
        LoginUserDto user = userService.getByUserName(username);
//        switch (loginType) {
//            case "web":
//                user = userService.getByUserName(username);
//                break;
//            case "email":
//                user = userService.getByEmail(username);
//                break;
//            case "phone":
//                user = userService.getByPhoneNumber(username);
//                break;
//            default:
//                return ApiResult.fail("无效的登录类型");
//        }
//        if (user == null) {
//            return ApiResult.fail("用户不存在");
//        }
        // 这里可以添加角色和权限信息
        return ApiResult.success(user);
    }
}
