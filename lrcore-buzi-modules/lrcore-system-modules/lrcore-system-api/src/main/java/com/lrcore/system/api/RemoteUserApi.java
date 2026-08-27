package com.lrcore.system.api;

import com.lrcore.common.core.constant.ServiceNameConstants;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.login.LoginUserDto;
import com.lrcore.system.api.factory.RemoteUserFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 系统服务模块
 * 1.contextId ：服务调用方名称，默认为服务名， 同服务多Feign冲突
 * 2.value: 用常量
 * 3.fallbackFactory: 容错处理，必须配置降级服务
 * @ClassName: RemoteUserApi
 * @Author: Qi Liu
 * @Date: 2026/4/3 09:13
 * @Version: 1.0
 */
@FeignClient(contextId = "remoteUserApi", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteUserFallbackFactory.class)
public interface RemoteUserApi {
    /**
     * 通过用户名查询用户信息
     *
     * @param username  用户名
     * @return 结果
     */
    @GetMapping("/user/info/{username}")
    ApiResult<LoginUserDto> getUserInfo(@PathVariable String username);
}
