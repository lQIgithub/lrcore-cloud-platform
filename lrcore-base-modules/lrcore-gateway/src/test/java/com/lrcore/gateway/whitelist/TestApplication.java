package com.lrcore.gateway.whitelist;

import com.lrcore.gateway.config.GatewaySecurityConfig;
import com.lrcore.gateway.config.properties.IgnoreWhiteProperties;
import com.lrcore.gateway.config.properties.Oauth2ServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 测试专用最小应用——仅装配生产 GatewaySecurityConfig（含 IgnoreWhiteProperties /
 *            Oauth2ServerProperties），不依赖 Nacos、注册中心与网关路由，
 *            用于验证“Bearer 令牌校验失败后白名单路径仍可放行”的核心修复语义。
 *
 * @ClassName: TestApplication
 * @Author: lrcore
 * @Date: 2026/8/21
 * @Version: 1.0
 */
@SpringBootApplication(exclude = {
        // 测试不连数据库（数据源配置来自 Nacos，测试环境不可用）
        org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration.class,
        org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration.class
}, excludeName = {
        // 测试禁用网关服务器后，Gateway 的 Redis 限流自动装配缺少 ConfigurationService，排除之
        "org.springframework.cloud.gateway.config.GatewayRedisAutoConfiguration"
})
@Import({
        GatewaySecurityConfig.class,
        IgnoreWhiteProperties.class,
        Oauth2ServerProperties.class
})
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    /**
     * 终端 WebFilter：请求“穿过”安全过滤链后写入 PASSED（模拟下游服务响应）。
     * 若安全链已提前应答（401），本过滤器不会被执行，响应保持 401 JSON。
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    WebFilter passedThroughMarker() {
        return (exchange, chain) -> exchange.getResponse()
                .writeWith(Mono.just(exchange.getResponse().bufferFactory()
                        .wrap("PASSED".getBytes(StandardCharsets.UTF_8))));
    }
}
