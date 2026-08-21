package com.lrcore.auth.handler;

import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.web.domain.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 认证中心全局异常处理。
 * <p>
 * lrcore-auth 不依赖 lrcore-common-web（其 GlobalExceptionHandler 仅随 common-web 装配），
 * 本处理器补齐认证中心自身的异常 → 平台 ApiResult 约定映射：
 * <ul>
 *   <li>ServiceException → HTTP 200 + ApiResult{code: 错误码（默认 "500"）, message}（与 common-web 行为一致）；</li>
 *   <li>参数校验异常 → HTTP 200 + ApiResult{code:"500", message=首个字段错误}；</li>
 *   <li>其余异常 → HTTP 200 + ApiResult{code:"500", message="系统繁忙，请稍后再试"}（明细只进日志，不外泄）。</li>
 * </ul>
 * 平台所有业务错误统一为「HTTP 200 + 业务 code」，与网关 401（HTTP 200 + code "401"）约定一致。
 * @ClassName: LrcoreAuthExceptionHandler
 * @Author: Qi Liu
 * @Date: 2026/8/20
 * @Version: 1.0
 */
@Slf4j
@RestControllerAdvice
public class LrcoreAuthExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ApiResult<String> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error("请求地址'{}',业务异常'{}'", request.getRequestURI(), e.getErrorMessage(), e);
        String code = e.getErrorCode();
        return code == null || code.isBlank()
                ? ApiResult.fail(e.getErrorMessage())
                : ApiResult.fail(code, e.getErrorMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ApiResult<String> handleValidationException(Exception e, HttpServletRequest request) {
        log.error("请求地址'{}',参数校验失败", request.getRequestURI(), e);
        String message = e instanceof MethodArgumentNotValidException ex
                ? ex.getBindingResult().getFieldErrors().stream().findFirst()
                        .map(fe -> fe.getField() + " " + fe.getDefaultMessage()).orElse("参数校验失败")
                : "参数校验失败";
        return ApiResult.fail(message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResult<String> handleException(Exception e, HttpServletRequest request) {
        log.error("请求地址'{}',发生系统异常", request.getRequestURI(), e);
        return ApiResult.fail("系统繁忙，请稍后再试");
    }
}
