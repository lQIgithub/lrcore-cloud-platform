package com.lrcore.auth.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lrcore.auth.domain.SocialAuthorizeResult;
import com.lrcore.auth.domain.SocialCallbackResult;
import com.lrcore.auth.domain.SsoTokenDto;
import com.lrcore.common.auth.social.SocialAccountBinding;
import com.lrcore.common.auth.social.SocialAccountBindingRepository;
import com.lrcore.common.auth.social.SocialAccountBindingService;
import com.lrcore.common.auth.social.SocialPlatform;
import com.lrcore.common.auth.social.SocialPlatformClient;
import com.lrcore.common.auth.social.SocialTokenInfo;
import com.lrcore.common.auth.social.SocialUserInfo;
import com.lrcore.common.auth.user.LrcoreUserDetails;
import com.lrcore.common.auth.user.LrcoreUserDetailsService;
import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.ip.IpUtils;
import com.lrcore.common.redis.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 第三方（微信扫码等）登录服务 —— 全面接入 SSO/SAS 体系。
 * <p>
 * 编排（复用 lrcore-common-auth 自动装配的社交客户端/绑定仓库/绑定服务）：
 * <ol>
 *   <li>authorize：生成一次性 state 写入 Redis（TTL 10 分钟），返回平台授权页 URL（前端 iframe 渲染二维码）；</li>
 *   <li>callback：校验并消费 state（防 CSRF/重放），授权码换平台令牌 → 拉取平台用户信息 →
 *       查绑定：已绑定则按绑定用户名走「与账号密码登录完全相同」的 SAS 强认证出令牌链路；
 *       未绑定则写入短时 pending 信息并回传 openId/昵称供前端展示绑定表单；</li>
 *   <li>bind：消费 pending，经 {@link LrcoreUserDetailsService} 加载平台用户（账户状态校验）
 *       并用 {@link PasswordEncoder} 校验本地凭据（与登录同策略），写入绑定，
 *       由 {@link SasAccessTokenIssuer} 签发 SAS JWT 访问令牌。</li>
 * </ol>
 * 出令牌链路全程由 SAS（Spring Authorization Server）的 {@code JwtGenerator} 完成，
 * 与 /oauth2/authorize 授权码流程签发的令牌同为 RS256、JWKS 公钥验签、claims 契约一致，
 * 彻底移除若依 HS512 双 Token 的 TokenService/SysPasswordService 签发。
 * @ClassName: SocialLoginService
 * @Author: Qi Liu
 * @Date 2026/8/24
 * @Version 2.0
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

    /** 社交登录默认授权范围（与 SPA 主登录客户端一致） */
    private static final Set<String> DEFAULT_SCOPES = Set.of("openid", "profile");

    private static final String SOCIAL_STATE_KEY = "social:state:";
    private static final String SOCIAL_PENDING_KEY = "social:pending:";

    private final Map<SocialPlatform, SocialPlatformClient> socialPlatformClients;

    private final SocialAccountBindingRepository socialAccountBindingRepository;

    private final SocialAccountBindingService socialAccountBindingService;

    private final LrcoreUserDetailsService lrcoreUserDetailsService;

    private final PasswordEncoder passwordEncoder;

    private final SasAccessTokenIssuer sasAccessTokenIssuer;

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
            SsoTokenDto tokenDto = issueTokenByUsername(binding.username());
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
     * 绑定本地账号并直接登录：经 {@link LrcoreUserDetailsService} 加载平台用户（账户状态校验）
     * → {@link PasswordEncoder} 校验本地密码 → 写绑定 → {@link SasAccessTokenIssuer} 签发 SAS 令牌。
     */
    public SsoTokenDto bind(String pendingToken, String platformCode, String username, String password) {
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

        // 平台用户强认证：加载（含账户状态）+ 密码校验（与账号密码登录完全同策略）
        LrcoreUserDetails userDetails = authenticateLocalAccount(username, password);

        // 写入绑定（幂等：已绑定本人则同步资料；绑定他人则拒绝）
        socialAccountBindingService.bind(userDetails, userInfo);

        SsoTokenDto tokenDto = toSsoToken(sasAccessTokenIssuer.issueAccessToken(userDetails, DEFAULT_SCOPES));
        log.info("第三方绑定并登录成功, platform: {}, username: {}", platform, userDetails.getUsername());
        return tokenDto;
    }

    // ==================== 私有辅助 ====================

    /**
     * 平台账号强认证：经 {@link LrcoreUserDetailsService} 加载（含停用/锁定校验），
     * 再用 {@link PasswordEncoder} 校验密码（BCrypt）。失败统一抛"绑定凭据错误"，
     * 不区分用户不存在/密码错误。
     *
     * @throws ServiceException 用户不存在 / 账户停用 / 账户锁定 / 密码错误
     */
    private LrcoreUserDetails authenticateLocalAccount(String username, String password) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            throw new ServiceException("用户名或密码不能为空");
        }
        LrcoreUserDetails userDetails = (LrcoreUserDetails) lrcoreUserDetailsService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new ServiceException("账号或密码错误");
        }
        return userDetails;
    }

    private SsoTokenDto issueTokenByUsername(String username) {
        LrcoreUserDetails userDetails = (LrcoreUserDetails) lrcoreUserDetailsService.loadUserByUsername(username);
        return toSsoToken(sasAccessTokenIssuer.issueAccessToken(userDetails, DEFAULT_SCOPES));
    }

    private static SsoTokenDto toSsoToken(org.springframework.security.oauth2.core.OAuth2AccessToken accessToken) {
        Long expiresIn = accessToken.getExpiresAt() == null
                ? null
                : Math.max(0, java.time.Duration.between(java.time.Instant.now(), accessToken.getExpiresAt()).getSeconds());
        return SsoTokenDto.builder()
                .accessToken(accessToken.getTokenValue())
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .build();
    }

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