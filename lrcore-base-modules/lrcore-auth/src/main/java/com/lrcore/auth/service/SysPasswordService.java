package com.lrcore.auth.service;

import com.lrcore.common.core.constant.CacheConstants;
import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.common.core.web.domain.login.LoginUserDto;
import com.lrcore.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 登录密码方法
 * @ClassName: SysPasswordService
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:54
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysPasswordService {
    private int maxRetryCount = CacheConstants.PASSWORD_MAX_RETRY_COUNT;
    private Long lockTime = CacheConstants.PASSWORD_LOCK_TIME;
    private final RedisService redisService;

    /**
     * 登录账户密码错误次数缓存键名
     *
     * @param username 用户名
     * @return 缓存键key
     */
    private String getCacheKey(String username) {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    public void validate(LoginUserDto user, String password) {
        String username = user.getUserName();
        Integer retryCount = redisService.getCacheObject(getCacheKey(username));
        if (retryCount == null) {
            retryCount = 0;
        }

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue()) {
            String errMsg = String.format("密码输入错误%s次，帐户锁定%s分钟", maxRetryCount, lockTime);
            throw new ServiceException(errMsg);
        }

        if (!matches(password, user.getPassword())) {
            retryCount = retryCount + 1;
            redisService.setCacheObject(getCacheKey(username), retryCount, lockTime, TimeUnit.MINUTES);
            throw new ServiceException("用户不存在/密码错误");
        } else {
            clearLoginRecordCache(username);
        }
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return SecurityUtils.matchesPassword(rawPassword, encodedPassword);
    }

    public void clearLoginRecordCache(String loginName) {
        if (redisService.hasKey(getCacheKey(loginName))) {
            redisService.deleteObject(getCacheKey(loginName));
        }
    }
}
