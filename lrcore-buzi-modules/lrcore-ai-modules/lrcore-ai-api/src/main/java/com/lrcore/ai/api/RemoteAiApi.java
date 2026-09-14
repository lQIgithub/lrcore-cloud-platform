package com.lrcore.ai.api;

import com.lrcore.ai.api.factory.RemoteAiFallbackFactory;
import com.lrcore.common.core.constant.ServiceNameConstants;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 系统服务模块
 * 1.contextId ：服务调用方名称，默认为服务名， 同服务多Feign冲突
 * 2.value: 用常量
 * 3.fallbackFactory: 容错处理，必须配置降级服务
 * @ClassName: RemoteAiApi
 * @Author: Qi Liu
 * @Date: 2026/9/14 22:55
 * @Version: 1.0
 */
@FeignClient(contextId = "remoteAiApi", value = ServiceNameConstants.AI_SERVICE, fallbackFactory = RemoteAiFallbackFactory.class)
public interface RemoteAiApi {

}
