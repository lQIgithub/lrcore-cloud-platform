package com.lrcore.system.api;

import com.lrcore.common.core.constant.ServiceNameConstants;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.api.dto.OAuth2ClientDto;
import com.lrcore.system.api.dto.OAuth2ClientSaveDto;
import com.lrcore.system.api.factory.RemoteOAuth2ClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 认证中心 —— OAuth2 客户端管理远程接口。
 *            后台管理系统（lrcore-system）对外提供管理员入口后，经本 Feign 调用
 *            lrcore-auth 的 {@code /api/v1/client/admin/**}（内部服务专用，
 *            由 feign 拦截器自动携带 {@code from-source: inner} 请求头通过
 *            {@code @InnerAuth} 校验）。
 * <pre>
 * 1. contextId：服务调用方名称，默认为服务名，同服务多 Feign 冲突需显式指定；
 * 2. value：用常量（ServiceNameConstants.AUTH_SERVICE）；
 * 3. fallbackFactory：容错处理，必须配置降级服务。
 * </pre>
 * @ClassName: RemoteOAuth2ClientApi
 * @Author: lrcore
 * @Date 2026/8/27
 * @Version 1.0
 */
@FeignClient(contextId = "remoteOAuth2ClientApi", value = ServiceNameConstants.AUTH_SERVICE,
        fallbackFactory = RemoteOAuth2ClientFallbackFactory.class)
public interface RemoteOAuth2ClientApi {

    /**
     * 注册 OAuth2 客户端。
     *
     * @param form 注册入参
     * @return 已注册的 clientId
     */
    @PostMapping("/api/v1/client/admin/register")
    ApiResult<String> register(@RequestBody OAuth2ClientSaveDto form);

    /**
     * 更新 OAuth2 客户端。
     */
    @PutMapping("/api/v1/client/admin/{clientId}")
    ApiResult<Boolean> update(@PathVariable("clientId") String clientId,
                              @RequestBody OAuth2ClientSaveDto form);

    /**
     * 删除 OAuth2 客户端。
     */
    @DeleteMapping("/api/v1/client/admin/{clientId}")
    ApiResult<Boolean> delete(@PathVariable("clientId") String clientId);

    /**
     * 查询客户端列表（含注释字段，便于展示）。
     */
    @GetMapping("/api/v1/client/admin/list")
    ApiResult<List<OAuth2ClientDto>> list();

    /**
     * 按 clientId 查询详情。
     */
    @GetMapping("/api/v1/client/admin/{clientId}")
    ApiResult<OAuth2ClientDto> detail(@PathVariable("clientId") String clientId);

    /**
     * 启用客户端。
     */
    @PostMapping("/api/v1/client/admin/{clientId}/enable")
    ApiResult<Boolean> enable(@PathVariable("clientId") String clientId);

    /**
     * 禁用客户端。
     */
    @PostMapping("/api/v1/client/admin/{clientId}/disable")
    ApiResult<Boolean> disable(@PathVariable("clientId") String clientId);
}