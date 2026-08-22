package com.lrcore.auth.controller;

import com.lrcore.common.core.constant.CacheConstants;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.redis.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSO 登录页验证码（AWT 绘制，零第三方依赖）。
 *            存储约定与平台既有链路完全一致：Redis key = {@code captcha_codes:{uuid}}
 *            （与网关 Kaptcha 验证码、starter RedisCaptchaVerifier 共用同一约定，
 *            先删后比、一次性、大小写不敏感）。
 * <p>
 * 登录页同源调用（登录页由本服务渲染，无跨域），返回
 * {@code {uuid, img: data:image/png;base64,...}}。
 * @ClassName SsoCaptchaController
 * @Author lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@Slf4j
@Tag(name = "SSO 登录")
@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
public class SsoCaptchaController {

    /**
     * 验证码有效期（分钟）
     */
    private static final long CAPTCHA_EXPIRE_MINUTES = 2L;

    private static final int IMAGE_WIDTH = 160;
    private static final int IMAGE_HEIGHT = 60;
    private static final int CODE_LENGTH = 4;

    /**
     * 去掉易混淆字符（0/O、1/I/L）
     */
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    private final RedisService redisService;

    @Operation(summary = "获取 SSO 登录验证码")
    @GetMapping("/captcha")
    public ApiResult<Map<String, String>> captcha() {
        String code = randomCode();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        redisService.setCacheObject(CacheConstants.CAPTCHA_CODE_KEY + uuid, code,
                CAPTCHA_EXPIRE_MINUTES, TimeUnit.MINUTES);
        String dataUrl = "data:image/png;base64,"
                + Base64.getEncoder().encodeToString(render(code));
        return ApiResult.success(Map.of("uuid", uuid, "img", dataUrl));
    }

    private static String randomCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * AWT 绘制验证码图片（随机背景 + 干扰线 + 逐字随机字体/旋转）。
     */
    static byte[] render(String code) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            Random random = new Random();
            // 背景
            g.setColor(new Color(245, 247, 250));
            g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
            // 干扰线
            for (int i = 0; i < 5; i++) {
                g.setColor(new Color(150 + random.nextInt(80), 150 + random.nextInt(80), 150 + random.nextInt(80)));
                g.setStroke(new BasicStroke(1.5f));
                g.drawLine(random.nextInt(IMAGE_WIDTH), random.nextInt(IMAGE_HEIGHT),
                        random.nextInt(IMAGE_WIDTH), random.nextInt(IMAGE_HEIGHT));
            }
            // 逐字绘制（随机颜色/字号/旋转）
            int charWidth = (IMAGE_WIDTH - 20) / code.length();
            for (int i = 0; i < code.length(); i++) {
                g.setColor(new Color(30 + random.nextInt(100), 30 + random.nextInt(100), 30 + random.nextInt(100)));
                int fontSize = 38 + random.nextInt(8);
                g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
                float x = 12f + i * charWidth + random.nextInt(6);
                float y = 42f + random.nextInt(8);
                double angle = (random.nextDouble() - 0.5) * 0.4;
                g.rotate(angle, x, y);
                g.drawString(String.valueOf(code.charAt(i)), x, y);
                g.rotate(-angle, x, y);
            }
            // 噪点
            for (int i = 0; i < 60; i++) {
                g.setColor(new Color(180 + random.nextInt(60), 180 + random.nextInt(60), 180 + random.nextInt(60)));
                g.fillOval(random.nextInt(IMAGE_WIDTH), random.nextInt(IMAGE_HEIGHT), 1, 1);
            }
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", out);
        } catch (Exception ex) {
            log.error("验证码图片渲染失败", ex);
            throw new IllegalStateException("验证码图片渲染失败", ex);
        }
        return out.toByteArray();
    }
}
