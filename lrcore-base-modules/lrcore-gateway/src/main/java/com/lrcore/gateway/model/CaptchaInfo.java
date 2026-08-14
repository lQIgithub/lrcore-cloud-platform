package com.lrcore.gateway.model;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>类模块说明</p>

 * @Describe: 提交表单登录
 * @ClassName: LoginBody
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:56
 * @Version: 1.0
 */
@Data
public class CaptchaInfo implements Serializable {
    /**
     * 验证码缓存 key
     */
    private String captchaId;
    /**
     * 验证码图片 Base64
     */
    private String captchaBase64;
}
