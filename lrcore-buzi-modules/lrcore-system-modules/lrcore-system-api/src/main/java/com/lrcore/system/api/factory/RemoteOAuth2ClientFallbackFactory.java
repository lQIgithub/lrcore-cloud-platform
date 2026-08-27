package com.lrcore.system.api.factory;

import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.api.RemoteOAuth2ClientApi;
import com.lrcore.system.api.dto.OAuth2ClientDto;
import com.lrcore.system.api.dto.OAuth2ClientSaveDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: OAuth2 客户端管理服务降级处理。
 *            认证中心（lrcore-auth）不可用时返回失败结果，后台管理系统「客户端管理」页
 *            以此给出降级提示（而非白屏/异常）。
 * @ClassName: RemoteOAuth2ClientFallbackFactory
 * @Author: lrcore
 * @Date 2026/8/27
 * @Version 1.0
 */
@Component
public class RemoteOAuth2ClientFallbackFactory implements FallbackFactory<RemoteOAuth2ClientApi> {

    private static final Logger log = LoggerFactory.getLogger(RemoteOAuth2ClientFallbackFactory.class);

    @Override
    public RemoteOAuth2ClientApi create(Throwable throwable) {
        log.error("OAuth2 客户端管理服务调用失败:{}", throwable.getMessage(), throwable);
        return new RemoteOAuth2ClientApi() {
            @Override
            public ApiResult<String> register(OAuth2ClientSaveDto form) {
                return ApiResult.fail("客户端注册服务调用失败:" + throwable.getMessage());
            }

            @Override
            public ApiResult<Boolean> update(String clientId, OAuth2ClientSaveDto form) {
                return ApiResult.fail("客户端更新服务调用失败:" + throwable.getMessage());
            }

            @Override
            public ApiResult<Boolean> delete(String clientId) {
                return ApiResult.fail("客户端删除服务调用失败:" + throwable.getMessage());
            }

            @Override
            public ApiResult<List<OAuth2ClientDto>> list() {
                return ApiResult.fail("客户端列表服务调用失败:" + throwable.getMessage());
            }

            @Override
            public ApiResult<OAuth2ClientDto> detail(String clientId) {
                return ApiResult.fail("客户端详情服务调用失败:" + throwable.getMessage());
            }

            @Override
            public ApiResult<Boolean> enable(String clientId) {
                return ApiResult.fail("客户端启用服务调用失败:" + throwable.getMessage());
            }

            @Override
            public ApiResult<Boolean> disable(String clientId) {
                return ApiResult.fail("客户端禁用服务调用失败:" + throwable.getMessage());
            }
        };
    }
}