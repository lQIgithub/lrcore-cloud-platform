package com.lrcore.gateway.service.impl;

import com.google.code.kaptcha.Producer;
import com.lrcore.common.core.constant.CacheConstants;
import com.lrcore.common.core.constant.Constants;
import com.lrcore.common.core.exception.CaptchaException;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.utils.sign.Base64;
import com.lrcore.common.core.utils.uuid.IdUtils;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.redis.service.RedisService;
import com.lrcore.gateway.config.properties.CaptchaProperties;
import com.lrcore.gateway.model.CaptchaInfo;
import com.lrcore.gateway.service.ValidateCodeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 验证码实现处理
 * @ClassName: ValidateCodeServiceImpl
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:50
 * @Version: 1.0
 */
@Service
public class ValidateCodeServiceImpl implements ValidateCodeService {
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CaptchaProperties captchaProperties;

    /**
     * 生成验证码
     */
    @Override
    public ApiResult<CaptchaInfo> createCaptcha() throws IOException, CaptchaException {
        boolean captchaEnabled = captchaProperties.getEnabled();
        if (!captchaEnabled) {
            return ApiResult.fail("验证码已关闭");
        }

        // 保存验证码信息
        String uuid = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        String capStr = null, code = null;
        BufferedImage image = null;

        String captchaType = captchaProperties.getType();
        // 生成验证码
        if ("math".equals(captchaType)) {
            String capText = captchaProducerMath.createText();
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            image = captchaProducerMath.createImage(capStr);
        } else if ("char".equals(captchaType)) {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }
        redisService.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            return ApiResult.fail(e.getMessage());
        }
        CaptchaInfo info = new CaptchaInfo();
        info.setCaptchaId(uuid);
        info.setCaptchaBase64("data:image/png;base64," + Base64.encode(os.toByteArray()));
        return ApiResult.success("获取验证码成功", info);
    }

    /**
     * 校验验证码
     */
    @Override
    public void checkCaptcha(String code, String uuid) throws CaptchaException {
        if (FunStrUtils.isEmpty(code)) {
            throw new CaptchaException("验证码不能为空");
        }
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + FunStrUtils.nvl(uuid, "");
        String captcha = redisService.getCacheObject(verifyKey);
        if (captcha == null) {
            throw new CaptchaException("验证码已失效");
        }
        // SADD 原子认领所提交的码：并发同码仅一个请求认领返回 1（其余返回 0），
        // 消除"读到即删"的并发窗口（与 MfaSmsCodeService 原子消费同方案）；
        // 比对忽略大小写，故认领小写码；错码仅消费错码本身，不影响正确码重试
        String usedKey = verifyKey + ":used";
        Long added = redisService.setCacheSet(usedKey, Set.<Object>of()).add(code.toLowerCase());
        if (added == null || added == 0L) {
            throw new CaptchaException("验证码错误");
        }
        redisService.expire(usedKey, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        if (!code.equalsIgnoreCase(captcha)) {
            throw new CaptchaException("验证码错误");
        }
        // 校验成功后删除码键（与原一次性语义一致）；消费声明键随 TTL 清理
        redisService.deleteObject(verifyKey);
    }
}
