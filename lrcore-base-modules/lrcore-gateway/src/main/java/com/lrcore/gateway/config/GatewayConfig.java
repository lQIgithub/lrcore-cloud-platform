package com.lrcore.gateway.config;

import com.lrcore.gateway.handler.SentinelFallbackHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 网关限流配置
 * @ClassName: GatewayConfig
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:57
 * @Version: 1.0
 */
@Configuration
public class GatewayConfig {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelFallbackHandler sentinelGatewayExceptionHandler() {
        return new SentinelFallbackHandler();
    }
}
