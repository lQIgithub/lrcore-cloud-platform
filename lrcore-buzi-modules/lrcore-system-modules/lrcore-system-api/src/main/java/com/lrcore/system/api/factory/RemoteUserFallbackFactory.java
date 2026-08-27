package com.lrcore.system.api.factory;

import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.login.LoginUserDto;
import com.lrcore.system.api.RemoteUserApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * 用户服务降级处理
 *
 * @author lrcore
 */
@Component
public class RemoteUserFallbackFactory implements FallbackFactory<RemoteUserApi> {
    private static final Logger log = LoggerFactory.getLogger(RemoteUserFallbackFactory.class);

    @Override
    public RemoteUserApi create(Throwable throwable) {
        log.error("用户服务调用失败:{}", throwable.getMessage());
        return new RemoteUserApi() {
            @Override
            public ApiResult<LoginUserDto> getUserInfo(String username) {
                return ApiResult.fail("获取用户失败:" + throwable.getMessage());
            }
        };
    }
}
