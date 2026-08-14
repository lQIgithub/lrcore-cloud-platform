package com.lrcore.flowable.api;

import com.lrcore.common.core.constant.ServiceNameConstants;
import com.lrcore.flowable.api.factory.RemoteFlowableFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;

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

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程服务模块
 * @ClassName: RemoteFlowableApi
 * @Author: Qi Liu
 * @Date: 2026/8/12 13:33
 * @Version: 1.0
 */
@FeignClient(contextId = "remoteFlowableApi", value = ServiceNameConstants.FLOWABLE_SERVICE, fallbackFactory = RemoteFlowableFallbackFactory.class)
public interface RemoteFlowableApi {

}
