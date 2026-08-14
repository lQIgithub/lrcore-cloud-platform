package com.lrcore.gateway.config;

import com.lrcore.gateway.handler.ValidateCodeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 路由配置信息
 * @ClassName: RouterFunctionConfiguration
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:54
 * @Version: 1.0
 */
@Configuration
@RequiredArgsConstructor
public class RouterFunctionConfiguration {
    private final ValidateCodeHandler validateCodeHandler;

    @SuppressWarnings("rawtypes")
    @Bean
    public RouterFunction routerFunction() {
        return RouterFunctions.route(
                RequestPredicates.GET("/api/v1/auth/captcha").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
                validateCodeHandler);
    }
}
