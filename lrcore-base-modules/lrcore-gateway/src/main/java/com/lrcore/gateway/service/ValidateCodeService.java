package com.lrcore.gateway.service;

import com.lrcore.common.core.exception.CaptchaException;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.gateway.model.CaptchaInfo;

import java.io.IOException;

/**
 * <p>类模块说明</p>
 *
 * @Describe:
 * @ClassName: ValidateCodeService
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:50
 * @Version: 1.0
 */
public interface ValidateCodeService {
    /**
     * 生成验证码
     */
    public ApiResult<CaptchaInfo> createCaptcha() throws IOException, CaptchaException;

    /**
     * 校验验证码
     */
    public void checkCaptcha(String key, String value) throws CaptchaException;
}
