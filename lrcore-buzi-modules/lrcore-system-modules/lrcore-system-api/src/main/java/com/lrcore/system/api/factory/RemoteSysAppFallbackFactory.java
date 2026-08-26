package com.lrcore.system.api.factory;

import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.api.RemoteSysAppApi;
import com.lrcore.system.api.dto.PortalAppDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 应用系统（子系统）门户清单服务降级处理。
 *            系统服务不可用时返回失败结果，认证中心门户以此展示降级提示（而非白屏/异常）。
 * @ClassName: RemoteSysAppFallbackFactory
 * @Author: lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@Component
public class RemoteSysAppFallbackFactory implements FallbackFactory<RemoteSysAppApi> {

    private static final Logger log = LoggerFactory.getLogger(RemoteSysAppFallbackFactory.class);

    @Override
    public RemoteSysAppApi create(Throwable throwable) {
        log.error("应用系统门户清单服务调用失败:{}", throwable.getMessage(), throwable);
        return () -> ApiResult.fail("获取子系统门户清单失败:" + throwable.getMessage());
    }
}