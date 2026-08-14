package com.lrcore.gateway.handler;

import com.lrcore.common.core.exception.CaptchaException;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.gateway.model.CaptchaInfo;
import com.lrcore.gateway.service.ValidateCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.io.IOException;

/**
 * 验证码获取
 *
 * @author lrcore
 */

/**
 * <p>类模块说明</p>
 *
 * @Describe: 验证码获取
 * @ClassName: ValidateCodeHandler
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:53
 * @Version: 1.0
 */
@Component
public class ValidateCodeHandler implements HandlerFunction<ServerResponse> {
    @Autowired
    private ValidateCodeService validateCodeService;

    @Override
    public Mono<ServerResponse> handle(ServerRequest serverRequest) {
        ApiResult<CaptchaInfo> ajax;
        try {
            ajax = validateCodeService.createCaptcha();
        } catch (CaptchaException | IOException e) {
            return Mono.error(e);
        }
        // 设置响应头类型
        return ServerResponse.status(HttpStatus.OK)
                .headers(headers -> {
                    headers.add("x-captcha-type", "image");
                })
                .body(BodyInserters.fromValue(ajax));
    }
}
