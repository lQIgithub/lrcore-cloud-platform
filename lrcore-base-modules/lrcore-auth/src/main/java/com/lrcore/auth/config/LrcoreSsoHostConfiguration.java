package com.lrcore.auth.config;

import com.lrcore.common.auth.api.LrcoreAuthenticator;
import com.lrcore.common.auth.provider.PasswordLrcoreAuthenticator;
import com.lrcore.common.auth.token.LrcoreTokenCustomizer;
import com.lrcore.common.auth.user.LrcoreUser;
import com.lrcore.common.auth.user.LrcoreUserDetailsService;
import com.lrcore.common.auth.user.LrcoreUserSource;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.login.LoginUserDto;
import com.lrcore.common.redis.service.RedisService;
import com.lrcore.system.api.RemoteUserApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

/**
 * <p>类模块说明</p>
 *
 * @Describe: SSO 单点登录 —— lrcore-auth 作为授权服务器（AS）宿主的装配。
 *            将平台用户库（经 Feign 调 lrcore-system）桥接为统一认证中心的
 *            用户数据源，并装配安全策略认证链与令牌 claims 定制器：
 * <ul>
 *   <li>{@link LrcoreUserSource}：RemoteUserApi.getUserInfo(username, "web") → {@link LrcoreUser}。
 *       用户不存在（远程查询失败/无数据）返回 null；roles 暂映射空集
 *       （平台 Feign 无角色查询接口，菜单权限由网关透传 user_id 后系统服务实时解析，
 *       与旧双 Token 链路行为一致；后续系统服务提供角色接口后在此补全）；</li>
 *   <li>{@link LrcoreUserDetailsService}：统一 UserDetailsService（替代脚手架内存用户），
 *       承载账户状态校验（停用/锁定）；</li>
 *   <li>{@link AuthenticationProvider}：{@link PasswordLrcoreAuthenticator}（withCaptcha 形态），
 *       固定策略链 IP黑名单 → IP防暴破 → 验证码 → 用户加载 → 账户锁定 → BCrypt；
 *       Spring Security 自动将其纳入容器 AuthenticationManager，
 *       供 SSO 登录过滤器（{@link LrcoreSsoLoginFilter}）调用；</li>
 *   <li>{@link LrcoreTokenCustomizer}：签发 JWT 时注入业务 claims
 *       （sub=user_id / user_id / username / roles / tenant_id / ent_id / dept_id），
 *       网关双轨鉴权的 sasPass 依赖该契约映射下游请求头。</li>
 * </ul>
 * PasswordEncoder 由 lrcore-common-core 的 PasswordEncoderAutoConfiguration 提供
 * （BCrypt, 强度 10），此处直接注入使用。
 * @ClassName LrcoreSsoHostConfiguration
 * @Author lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@Slf4j
@Configuration
public class LrcoreSsoHostConfiguration {

    /**
     * 平台用户数据源：经 lrcore-system-api 的 RemoteUserApi 远程加载并映射为 LrcoreUser。
     */
    @Bean
    public LrcoreUserSource lrcoreUserSource(RemoteUserApi remoteUserApi) {
        return username -> {
            ApiResult<LoginUserDto> result;
            try {
                result = remoteUserApi.getUserInfo(username, "web");
            } catch (Exception ex) {
                // 远程调用异常向上抛（由 LrcoreUserDetailsService 包装为内部错误，不泄露细节）
                throw ex;
            }
            if (result == null || !result.isSuccess() || result.getData() == null) {
                log.info("SSO 用户源: 用户不存在, username: {}", username);
                return null;
            }
            LoginUserDto dto = result.getData();
            return new LrcoreUser(dto.getUserId(),
                    dto.getUserName(),
                    dto.getPassword(),
                    Collections.emptyList(),
                    null,
                    dto.getTenantId(),
                    dto.getEnterpriseId(),
                    dto.getDeptId());
        };
    }

    /**
     * 统一用户详情服务（替代脚手架内存用户 Bean：其 @ConditionalOnMissingBean 自动让位）。
     */
    @Bean
    public UserDetailsService lrcoreUserDetailsService(LrcoreUserSource lrcoreUserSource) {
        return new LrcoreUserDetailsService(lrcoreUserSource);
    }

    /**
     * 密码认证器（平台安全策略链，含验证码校验）。
     * 注册为 AuthenticationProvider Bean 后自动纳入容器 AuthenticationManager。
     */
    @Bean
    public AuthenticationProvider lrcorePasswordAuthenticationProvider(
            UserDetailsService lrcoreUserDetailsService,
            PasswordEncoder passwordEncoder,
            RedisService redisService) {
        log.info("SSO 装配: PasswordLrcoreAuthenticator（withCaptcha，Redis 验证码约定 captcha_codes:{uuid}）");
        LrcoreAuthenticator authenticator = PasswordLrcoreAuthenticator.withCaptcha(
                lrcoreUserDetailsService, passwordEncoder, redisService);
        return authenticator;
    }

    /**
     * 令牌 claims 定制器：Bean 形式注册，SAS DSL 自动探测。
     */
    @Bean
    public LrcoreTokenCustomizer lrcoreTokenCustomizer() {
        return new LrcoreTokenCustomizer();
    }
}
