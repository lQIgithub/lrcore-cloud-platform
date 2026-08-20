package com.lrcore.auth.service;

import com.lrcore.auth.form.LoginForm;
import com.lrcore.common.core.constant.CacheConstants;
import com.lrcore.common.core.constant.Constants;
import com.lrcore.common.core.constant.GlobalConstants;
import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.FunConvertUtils;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.utils.JwtUtils;
import com.lrcore.common.core.utils.ip.IpUtils;
import com.lrcore.common.core.validators.BeanValidators;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.login.LoginUserDto;
import com.lrcore.common.redis.service.RedisService;
import com.lrcore.common.security.token.model.TokenDto;
import com.lrcore.common.security.token.service.TokenService;
import com.lrcore.system.api.RemoteUserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 登录校验方法
 * @ClassName: SysLoginService
 * @Author: Qi Liu
 * @Date: 2026/3/25 17:39
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLoginService {
    private final RemoteUserApi remoteUserApi;
    private final SysPasswordService passwordService;
    private final RedisService redisService;
    private final TokenService tokenService;

    public ApiResult<TokenDto> login(LoginForm loginForm) {
        // @formatter:off
        try {
            log.info("loginForm>>>>>>>登录用户信息:{}", loginForm);
            BeanValidators.validate(loginForm);
            String username = loginForm.getUsername();
            String password = loginForm.getPassword();

            // IP黑名单校验
            String ipAddr = IpUtils.getIpAddr();
            log.info("IP黑名单校验>>>>>>>登录IP:{}", ipAddr);
            String blackStr = FunConvertUtils.toStr(redisService.getCacheObject(CacheConstants.SYS_LOGIN_BLACKIPLIST));
            if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr())) {
                throw new ServiceException("很遗憾，访问IP已被列入系统黑名单");
            }
            // 防暴力破解：检查登录失败次数
            String loginFailKey = CacheConstants.LOGIN_FAIL_KEY + ipAddr;
            log.info("登录失败次数检查>>>>>>>登录失败次数检查:{}", loginFailKey);
            Integer failCount = redisService.getCacheObject(loginFailKey);
            log.info("登录失败次数检查>>>>>>>登录失败次数检查:{}", failCount);
            if (failCount != null && failCount >= 5) {
                throw new ServiceException("登录失败次数过多，请稍后再试");
            }
            // 调用远程SYSTEM模块获取用户信息
            ApiResult<LoginUserDto> userResult = remoteUserApi.getUserInfo(username, "web");
            log.info("remoteUserApi.getUserInfo>>>>>>>登录用户信息:{}", userResult);
            if (!userResult.isSuccess() || userResult.getData() == null) {
                throw new ServiceException(userResult);
            }
            LoginUserDto loginUserDto = userResult.getData();

            passwordService.validate(loginUserDto, password);

            // 创建token
            TokenDto tokenDto = tokenService.createToken(loginUserDto);
            log.info("createToken>>>>>>>登录用户生成token:{}", tokenDto);
            return ApiResult.success(Constants.SUCCESS, tokenDto);
        } catch (ServiceException e) {
            throw new ServiceException("登录失败" + e.getErrorMessage());
        }
        // @formatter:on
    }

    public ApiResult<TokenDto> refreshToken(String refreshToken) {
        // 1、验证刷新令牌
        if (FunStrUtils.isEmpty(refreshToken)) {
            throw new ServiceException("刷新令牌不能为空!");
        }
        boolean isValidToken = JwtUtils.verifyToken(refreshToken);
        if (!isValidToken) {
            throw new ServiceException("刷新令牌已过期!");
        }
        // 2、通过redis中获取对应的accessToken
        String refreshTokenKey = CacheConstants.REFRESH_TOKEN_KEY + JwtUtils.getUserKey(refreshToken);
        String accessToken = redisService.getCacheObject(refreshTokenKey);

        // 3、解析accessToken，获取对应的用户信息
        String username = JwtUtils.getUserName(accessToken);
        ApiResult<LoginUserDto> userResult = remoteUserApi.getUserInfo(username, "web");
        log.info("remoteUserApi.getUserInfo>>>>>>>登录用户信息:{}", userResult);
        if (!userResult.isSuccess() || userResult.getData() == null) {
            throw new ServiceException(userResult);
        }
        // 4、生成新的访问令牌
        LoginUserDto loginUserDto = userResult.getData();
        TokenDto tokenDto = tokenService.createToken(loginUserDto);

        // 5、获取到新的访问token及刷新token， 删除redis缓存中的旧token
        return ApiResult.success(tokenDto);
    }

    /**
     * 退出
     */
    public void logout(String loginName) {
    }

    /**
     * 解锁
     */
    public void unlock(String password) {
//        String username = SecurityUtils.getUsername();
//        // 或密码为空 错误
//        if (StringUtils.isEmpty(password)) {
//            throw new ServiceException("密码不能为空");
//        }
//        // 查询用户信息
//        ApiResult<LoginUserDto> userResult = remoteUserApi.getUserInfo(username, "username", SecurityConstants.INNER);
//
//        if (ApiResult.FAIL.equals(userResult.getCode())) {
//            throw new ServiceException(userResult.getMessage());
//        }

//        SysUserDto user = userResult.getData().getSysUserDto();
//        if (!SecurityUtils.matchesPassword(password, user.getPassword())) {
//            throw new ServiceException("密码错误，请重新输入");
//        }
    }

    /**
     * 记录登录失败次数
     */
    private void recordLoginFailure() {
        String ipAddr = IpUtils.getIpAddr();
        String loginFailKey = CacheConstants.LOGIN_FAIL_KEY + ipAddr;
        Integer failCount = redisService.getCacheObject(loginFailKey);
        if (failCount == null) {
            failCount = 0;
        }
        failCount++;
        redisService.setCacheObject(loginFailKey, failCount, 1L, java.util.concurrent.TimeUnit.HOURS);
    }

    /**
     * 清除登录失败次数
     */
    private void clearLoginFailure() {
        String ipAddr = IpUtils.getIpAddr();
        String loginFailKey = CacheConstants.LOGIN_FAIL_KEY + ipAddr;
        redisService.deleteObject(loginFailKey);
    }

    /**
     * 注册
     */
    public void register(String username, String password) {
        // 用户名或密码为空 错误
        if (FunStrUtils.isAnyBlank(username, password)) {
            throw new ServiceException("用户/密码必须填写");
        }
        if (username.length() < GlobalConstants.USERNAME_MIN_LENGTH || username.length() > GlobalConstants.USERNAME_MAX_LENGTH) {
            throw new ServiceException("账户长度必须在2到20个字符之间");
        }
        if (password.length() < GlobalConstants.PASSWORD_MIN_LENGTH || password.length() > GlobalConstants.PASSWORD_MAX_LENGTH) {
            throw new ServiceException("密码长度必须在5到20个字符之间");
        }

        // 注册用户信息
//        SysUserDto sysUserDto = new SysUserDto();
//        sysUserDto.setUserName(username);
//        sysUserDto.setNickName(username);
//        sysUserDto.setPwdUpdateDate(DateUtils.nowDateTime());
//        sysUserDto.setPassword(SecurityUtils.encryptPassword(password));
//        ApiResult<?> registerResult = remoteUserApi.registerUserInfo(sysUserDto, SecurityConstants.INNER);

//        if (ApiResult.FAIL.equals(registerResult.getCode())) {
//            throw new ServiceException(Constants.FAIL, registerResult.getMessage(), registerResult.getErrorStack());
//        }
    }

}
