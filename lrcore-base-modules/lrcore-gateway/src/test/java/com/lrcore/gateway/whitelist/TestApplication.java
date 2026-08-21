package com.lrcore.gateway.whitelist;

import com.lrcore.common.redis.service.RedisService;
import com.lrcore.gateway.config.GatewaySecurityConfig;
import com.lrcore.gateway.config.properties.IgnoreWhiteProperties;
import com.lrcore.gateway.config.properties.Oauth2ServerProperties;
import com.lrcore.gateway.filter.AuthFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 测试专用最小应用——装配生产的 GatewaySecurityConfig（安全链）与 AuthFilter（双轨鉴权），
 *            按生产顺序运行（AuthFilter -200 → 安全链 -100 → 终端标记过滤器），
 *            用于验证“Bearer 令牌校验失败后白名单路径仍可放行”“内部来源标记防伪造”
 *            与“旧链路合法令牌注入用户头”等核心修复语义。不依赖 Nacos、注册中心与真实 Redis。
 *
 * @ClassName: TestApplication
 * @Author: lrcore
 * @Date: 2026/8/21
 * @Version: 1.1
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
        Oauth2ServerProperties.class,
        AuthFilter.class
})
public class TestApplication {

    /**
     * 记录到达终端过滤器的请求头（单值 Map，用于断言网关的头部注入/清除行为）。
     */
    public static final AtomicReference<Map<String, String>> LAST_FORWARDED_HEADERS = new AtomicReference<>();

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    /**
     * 终端 WebFilter：请求“穿过”全部前置过滤器（安全链 + AuthFilter）后写入 PASSED（模拟下游服务响应），
     * 并记录到达时的请求头。若前置过滤器已提前应答（401），本过滤器不会被执行。
     */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    WebFilter passedThroughMarker() {
        return (exchange, chain) -> {
            LAST_FORWARDED_HEADERS.set(exchange.getRequest().getHeaders().toSingleValueMap());
            return exchange.getResponse()
                    .writeWith(Mono.just(exchange.getResponse().bufferFactory()
                            .wrap("PASSED".getBytes(StandardCharsets.UTF_8))));
        };
    }

    /**
     * 测试环境禁用了 Gateway 自动装配，而生产环境中 GlobalFilter（AuthFilter）是由 Spring Cloud
     * Gateway 的 WebHandler 定制器织入请求链的（GlobalFilter 并非 WebFilter 子类型，需适配）；
     * 此处显式适配并注册为 WebFilter，order=-200 与生产一致（早于安全链 -100）。
     */
    @Bean
    @Order(-200)
    WebFilter authFilterWebFilter(AuthFilter authFilter) {
        return (exchange, chain) -> authFilter.filter(exchange, chain::filter);
    }

    /**
     * 测试用 Redis 登录态服务：hasKey 恒为 true（模拟“旧双令牌链路存在有效登录态”）。
     * 不初始化 Redis 连接：测试路径中除 hasKey（已覆写）外不会实际访问 Redis。
     */
    @Bean
    RedisService redisService() {
        return new RedisService(null) {
            @Override
            public Boolean hasKey(String key) {
                return true;
            }
        };
    }
}
