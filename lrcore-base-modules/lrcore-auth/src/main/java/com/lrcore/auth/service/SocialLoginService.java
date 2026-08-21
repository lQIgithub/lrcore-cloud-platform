package com.lrcore.auth.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lrcore.auth.domain.SocialAuthorizeResult;
import com.lrcore.auth.domain.SocialCallbackResult;
import com.lrcore.common.auth.social.SocialAccountBinding;
import com.lrcore.common.auth.social.SocialAccountBindingRepository;
import com.lrcore.common.auth.social.SocialAccountBindingService;
import com.lrcore.common.auth.social.SocialPlatform;
import com.lrcore.common.auth.social.SocialPlatformClient;
import com.lrcore.common.auth.social.SocialTokenInfo;
import com.lrcore.common.auth.social.SocialUserInfo;
import com.lrcore.common.auth.user.LrcoreUser;
import com.lrcore.common.auth.user.LrcoreUserDetails;
import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.ip.IpUtils;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.login.LoginUserDto;
import com.lrcore.common.redis.service.RedisService;
import com.lrcore.common.security.token.model.TokenDto;
import com.lrcore.common.security.token.service.TokenService;
import com.lrcore.system.api.RemoteUserApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 第三方（微信扫码）登录服务。
 * <p>
 * 编排（复用 lrcore-common-auth 自动装配的社交客户端/绑定仓库/绑定服务）：
 * <ol>
 *   <li>authorize：生成一次性 state 写入 Redis（TTL 10 分钟），返回平台授权页 URL（前端 iframe 渲染二维码）；</li>
 *   <li>callback：校验并消费 state（防 CSRF/重放），授权码换平台令牌 → 拉取平台用户信息 →
 *       查绑定：已绑定则按绑定用户名走「与账号密码登录完全相同」的出令牌链路（remoteUserApi + tokenService）；
 *       未绑定则写入短时 pending 信息并回传 openId/昵称供前端展示绑定表单；</li>
 *   <li>bind：消费 pending，校验本地账号凭据（与登录同链路），写入绑定并直接出令牌。</li>
 * </ol>
 * 出令牌链路与 {@link SysLoginService#login} 完全一致，保证网关 AuthFilter / 资源服务器无感知。
 * @ClassName: SocialLoginService
 * @Author: Qi Liu
 * @Date: 2026/8/20
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "lrcore.auth.social", name = "enabled", havingValue = "true")
public class SocialLoginService {

    /** state 有效期（分钟）：覆盖扫码 + 回调全流程 */
    private static final long STATE_TTL_MINUTES = 10;

    /** 未绑定 pending 有效期（分钟）：给用户填绑定表单留足时间 */
    private static final long PENDING_TTL_MINUTES = 10;

    private static final String SOCIAL_STATE_KEY = "social:state:";
    private static final String SOCIAL_PENDING_KEY = "social:pending:";

    private final Map<SocialPlatform, SocialPlatformClient> socialPlatformClients;

    private final SocialAccountBindingRepository socialAccountBindingRepository;

    private final SocialAccountBindingService socialAccountBindingService;

    private final RemoteUserApi remoteUserApi;

    private final SysPasswordService sysPasswordService;

    private final TokenService tokenService;

    private final RedisService redisService;

    /**
     * 发起第三方授权：生成 state 并返回平台授权页 URL。
     */
    public SocialAuthorizeResult authorize(String platformCode) {
        SocialPlatform platform = requirePlatform(platformCode);
        SocialPlatformClient client = requireClient(platform);

        String state = UUID.randomUUID().toString().replace("-", "");
        redisService.setCacheObject(SOCIAL_STATE_KEY + state, platform.getCode(),
                STATE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("第三方授权发起, platform: {}, state: {}, ip: {}", platform, state, safeIp());

        SocialAuthorizeResult result = new SocialAuthorizeResult();
        result.setPlatform(platform.getCode());
        result.setState(state);
        result.setAuthorizeUrl(client.buildAuthorizeUrl(null, state));
        return result;
    }

    /**
     * 处理平台回调（code + state）：已绑定出令牌，未绑定回传 pending 供绑定。
     */
    public SocialCallbackResult callback(String platformCode, String code, String state) {
        SocialPlatform platform = requirePlatform(platformCode);
        requireClient(platform);

        if (StrUtil.isBlank(code)) {
            throw new ServiceException("平台授权码缺失");
        }
        if (StrUtil.isBlank(state)) {
            throw new ServiceException("state 缺失，请重新发起扫码");
        }
        // 校验并消费 state（一次性，防 CSRF / 重放）
        String key = SOCIAL_STATE_KEY + state;
        String expectedPlatform = redisService.getCacheObject(key);
        if (StrUtil.isBlank(expectedPlatform) || !expectedPlatform.equals(platform.getCode())) {
            log.warn("第三方回调 state 校验失败, platform: {}", platform);
            throw new ServiceException("登录状态已失效，请重新扫码");
        }
        redisService.deleteObject(key);

        // 授权码换平台令牌 → 拉取平台用户信息
        SocialPlatformClient client = socialPlatformClients.get(platform);
        SocialTokenInfo tokenInfo;
        try {
            tokenInfo = client.exchangeCode(code, null);
        } catch (RuntimeException ex) {
            log.error("第三方授权码交换失败, platform: {}", platform, ex);
            throw new ServiceException("第三方登录失败，请重试");
        }
        SocialUserInfo userInfo;
        try {
            userInfo = client.fetchUserInfo(tokenInfo);
        } catch (RuntimeException ex) {
            log.error("第三方用户信息获取失败, platform: {}", platform, ex);
            throw new ServiceException("第三方登录失败，请重试");
        }
        if (userInfo == null || StrUtil.isBlank(userInfo.openId())) {
            throw new ServiceException("第三方登录失败，请重试");
        }

        SocialCallbackResult result = new SocialCallbackResult();
        result.setPlatform(platform.getCode());
        result.setOpenId(userInfo.openId());
        result.setNickname(userInfo.nickname());
        result.setAvatarUrl(userInfo.avatarUrl());

        // 绑定查询：命中 → 出令牌；未命中 → 写 pending，引导绑定
        SocialAccountBinding binding =
                socialAccountBindingRepository.findByPlatformAndOpenId(platform, userInfo.openId());
        if (binding != null) {
            log.info("第三方登录已绑定, platform: {}, username: {}", platform, binding.username());
            TokenDto tokenDto = issueTokenByUsername(binding.username());
            result.setBound(true);
            result.setToken(tokenDto);
            return result;
        }

        String pendingToken = UUID.randomUUID().toString().replace("-", "");
        redisService.setCacheObject(SOCIAL_PENDING_KEY + pendingToken, pendingJson(userInfo),
                PENDING_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("第三方登录未绑定（首次），已生成绑定凭据, platform: {}, openId: {}", platform, userInfo.openId());
        result.setBound(false);
        result.setPendingToken(pendingToken);
        return result;
    }

    /**
     * 绑定本地账号并直接登录：校验凭据（与登录同链路）→ 写绑定 → 出令牌。
     */
    public TokenDto bind(String pendingToken, String platformCode, String username, String password) {
        SocialPlatform platform = requirePlatform(platformCode);
        if (StrUtil.isBlank(pendingToken)) {
            throw new ServiceException("绑定凭据不能为空");
        }
        String key = SOCIAL_PENDING_KEY + pendingToken;
        String pendingJson = redisService.getCacheObject(key);
        if (StrUtil.isBlank(pendingJson)) {
            throw new ServiceException("绑定会话已过期，请重新扫码");
        }
        JSONObject pending = JSONUtil.parseObj(pendingJson);
        SocialUserInfo userInfo = new SocialUserInfo(
                platform, pending.getStr("openId"), pending.getStr("unionId"),
                pending.getStr("nickname"), pending.getStr("avatarUrl"));
        if (StrUtil.isBlank(userInfo.openId())) {
            throw new ServiceException("绑定会话无效，请重新扫码");
        }
        redisService.deleteObject(key);

        // 本地凭据校验（与账号密码登录完全相同的链路：remoteUserApi + passwordService）
        LoginUserDto loginUserDto = fetchLoginUser(username);
        sysPasswordService.validate(loginUserDto, password);

        // 写入绑定（幂等：已绑定本人则同步资料；绑定他人则拒绝）
        LrcoreUser lrcoreUser = new LrcoreUser(
                loginUserDto.getUserId(), loginUserDto.getUserName(), loginUserDto.getPassword(),
                List.of(), null, loginUserDto.getTenantId(), loginUserDto.getEnterpriseId(), loginUserDto.getDeptId());
        socialAccountBindingService.bind(new LrcoreUserDetails(lrcoreUser), userInfo);

        TokenDto tokenDto = tokenService.createToken(loginUserDto);
        log.info("第三方绑定并登录成功, platform: {}, username: {}", platform, loginUserDto.getUserName());
        return tokenDto;
    }

    // ==================== 私有辅助 ====================

    /**
     * 平台用户信息 → Redis JSON（显式字段映射，规避 record 序列化差异）。
     */
    private static String pendingJson(SocialUserInfo userInfo) {
        JSONObject json = new JSONObject();
        json.set("platform", userInfo.platform() == null ? null : userInfo.platform().getCode());
        json.set("openId", userInfo.openId());
        json.set("unionId", userInfo.unionId());
        json.set("nickname", userInfo.nickname());
        json.set("avatarUrl", userInfo.avatarUrl());
        return json.toString();
    }

    private LoginUserDto fetchLoginUser(String username) {
        ApiResult<LoginUserDto> userResult = remoteUserApi.getUserInfo(username, "web");
        if (userResult == null || !userResult.isSuccess() || userResult.getData() == null) {
            throw new ServiceException(userResult == null ? "获取用户信息失败" : userResult.getMessage());
        }
        return userResult.getData();
    }

    private TokenDto issueTokenByUsername(String username) {
        LoginUserDto loginUserDto = fetchLoginUser(username);
        return tokenService.createToken(loginUserDto);
    }

    private SocialPlatform requirePlatform(String platformCode) {
        SocialPlatform platform = SocialPlatform.fromCode(platformCode);
        if (platform == null) {
            throw new ServiceException("不支持的第三方平台: " + platformCode);
        }
        return platform;
    }

    private SocialPlatformClient requireClient(SocialPlatform platform) {
        SocialPlatformClient client = socialPlatformClients.get(platform);
        if (client == null) {
            throw new ServiceException("第三方登录未启用: " + platform.getDescription());
        }
        return client;
    }

    private String safeIp() {
        try {
            return IpUtils.getIpAddr();
        } catch (RuntimeException ex) {
            return "unknown";
        }
    }
}
