package com.lrcore.ai.api.factory;

import com.lrcore.ai.api.RemoteAiApi;
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
public class RemoteAiFallbackFactory implements FallbackFactory<RemoteAiApi> {
    private static final Logger log = LoggerFactory.getLogger(RemoteAiFallbackFactory.class);

    @Override
    public RemoteAiApi create(Throwable throwable) {
        log.error("AI服务调用失败:{}", throwable.getMessage());
        return new RemoteAiApi() {

        };
    }
}
