package com.lrcore.system.api;

import com.lrcore.common.core.constant.ServiceNameConstants;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.api.dto.PortalAppDto;
import com.lrcore.system.api.factory.RemoteSysAppFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 系统服务 —— 应用系统（子系统）门户清单远程接口。
 *            供认证中心（lrcore-auth）在单点登录成功后加载子门户页可进入的子系统列表。
 * <pre>
 * 1. contextId：服务调用方名称，默认为服务名，同服务多 Feign 冲突需显式指定；
 * 2. value：用常量（ServiceNameConstants.SYSTEM_SERVICE）；
 * 3. fallbackFactory：容错处理，必须配置降级服务。
 * </pre>
 * @ClassName: RemoteSysAppApi
 * @Author: lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@FeignClient(contextId = "remoteSysAppApi", value = ServiceNameConstants.SYSTEM_SERVICE, fallbackFactory = RemoteSysAppFallbackFactory.class)
public interface RemoteSysAppApi {

    /**
     * 查询启用中（status=0）的所有应用系统，用于门户展示。
     *
     * @return 门户应用条目列表
     */
    @GetMapping("/sysApp/portalList")
    ApiResult<List<PortalAppDto>> portalList();
}