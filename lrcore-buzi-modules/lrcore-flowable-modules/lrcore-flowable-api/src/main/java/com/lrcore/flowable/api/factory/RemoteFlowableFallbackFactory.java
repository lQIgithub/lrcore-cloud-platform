package com.lrcore.flowable.api.factory;

import com.lrcore.flowable.api.RemoteFlowableApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程服务降级处理
 * @ClassName: RemoteFlowableFallbackFactory
 * @Author: Qi Liu
 * @Date: 2026/8/12 13:33
 * @Version: 1.0
 */
public class RemoteFlowableFallbackFactory implements FallbackFactory<RemoteFlowableApi> {
    private static final Logger log = LoggerFactory.getLogger(RemoteFlowableFallbackFactory.class);

    @Override
    public RemoteFlowableApi create(Throwable throwable) {
        log.error("流程服务调用失败:{}", throwable.getMessage());

        return null;
    }
}
