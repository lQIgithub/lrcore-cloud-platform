package com.lrcore.gateway.config;

import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.lrcore.common.core.utils.FunStrUtils;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>类模块说明</p>
 *
 * @Describe: Swagger文档配置
 * @ClassName: SpringDocConfig
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:55
 * @Version: 1.0
 */
@RequiredArgsConstructor
@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "springdoc.api-docs.enabled", matchIfMissing = true)
public class SpringDocConfig implements InitializingBean {
    private final SwaggerUiConfigProperties swaggerUiConfigProperties;
    private final DiscoveryClient discoveryClient;

    /**
     * 在初始化后调用的方法
     */
    @Override
    public void afterPropertiesSet() {
        NotifyCenter.registerSubscriber(new SwaggerDocRegister(swaggerUiConfigProperties, discoveryClient));
    }
}

/**
 * Swagger文档注册器
 */
@RequiredArgsConstructor
class SwaggerDocRegister extends Subscriber<InstancesChangeEvent> {
    private final SwaggerUiConfigProperties swaggerUiConfigProperties;
    private final DiscoveryClient discoveryClient;

    // 过滤掉路由列表中排除的
//    private final static String[] EXCLUDE_ROUTES = new String[]{"lrcore-gateway", "lrcore-auth", "lrcore-file", "lrcore-monitor"};
    private final static String[] EXCLUDE_ROUTES = new String[]{"lrcore-gateway", "lrcore-monitor"};

    /**
     * 事件回调方法，处理InstancesChangeEvent事件
     *
     * @param event 事件对象
     */
    @Override
    public void onEvent(InstancesChangeEvent event) {
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> swaggerUrlSet = discoveryClient.getServices()
                .stream()
                .flatMap(serviceId -> discoveryClient.getInstances(serviceId).stream())
                .filter(instance -> !FunStrUtils.equalsAnyIgnoreCase(instance.getServiceId(), EXCLUDE_ROUTES))
                .map(instance -> {
                    AbstractSwaggerUiConfigProperties.SwaggerUrl swaggerUrl = new AbstractSwaggerUiConfigProperties.SwaggerUrl();
                    swaggerUrl.setName(instance.getServiceId());
                    swaggerUrl.setUrl(String.format("/%s/v3/api-docs", instance.getServiceId()));
                    return swaggerUrl;
                })
                .collect(Collectors.toSet());

        swaggerUiConfigProperties.setUrls(swaggerUrlSet);
        // 全部展开模型列表 Models 区域所有实体分类默认展开；0 = 全部折叠，1 = 默认
        swaggerUiConfigProperties.setDefaultModelsExpandDepth(-1);
        // 实体内部无限层级展开 单个实体内部递归展开深度，数字越大嵌套层级展开越多，99 等价无限展开
        swaggerUiConfigProperties.setDefaultModelExpandDepth(99);
        // 接口全部展开 接口标签、所有接口全部展开；list = 仅展开标签；none = 全部折叠。这里设置全部打开，会导致界面加载失败，接口很多的情况下有问题
        //swaggerUiConfigProperties.setDocExpansion("full");
    }

    /**
     * 订阅类型方法，返回订阅的事件类型
     *
     * @return 订阅的事件类型
     */
    @Override
    public Class<? extends Event> subscribeType() {
        return InstancesChangeEvent.class;
    }
}
