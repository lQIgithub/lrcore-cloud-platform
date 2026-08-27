package com.lrcore.auth.service;

import com.lrcore.system.api.dto.OAuth2ClientDto;
import com.lrcore.system.api.dto.OAuth2ClientSaveDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * <p>类模块说明</p>
 *
 * @Describe: OAuth2 客户端管理服务（平台内部，供后台管理系统经 Feign 调用）。
 *            <ul>
 *              <li>注册 / 更新 / 删除 / 启停 / 查询：基于 {@link RegisteredClientRepository} 的
 *                  标准能力（findByClientId / save / findById）；</li>
 *              <li>列表：标准 Repository 无 list 能力，故“尽力”使用 OAuth2 持久化
 *                  {@link JdbcTemplate}（lrcoreSsoJdbcTemplate）直查
 *                  {@code oauth2_registered_client} 表；data-source 未装配时返回空列表并告警。</li>
 *            </ul>
 *            形态：
 *            <ul>
 *              <li>公共 PKCE 客户端（SPA）：{@code publicClient=true}（默认）→ 无密钥、
 *                  强制 PKCE、免授权同意页；</li>
 *              <li>服务端客户端：{@code publicClient=false} → 必须填 {@code clientSecret}，
 *                  授权码 + 刷新令牌、需授权同意页。</li>
 *            </ul>
 * @ClassName: OAuth2ClientAdminService
 * @Author: lrcore
 * @Date: 2026/8/27
 * @Version: 1.0
 */
@Service
public class OAuth2ClientAdminService {

    private static final Logger log = LoggerFactory.getLogger(OAuth2ClientAdminService.class);

    /**
     * 客户端主密钥哈希编码器（客户端密钥与用户密码无关，固定使用 BCrypt）。
     */
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final RegisteredClientRepository registeredClientRepository;
    private final JdbcTemplate lrcoreSsoJdbcTemplate;

    public OAuth2ClientAdminService(RegisteredClientRepository registeredClientRepository,
                                    JdbcTemplate lrcoreSsoJdbcTemplate) {
        this.registeredClientRepository = registeredClientRepository;
        this.lrcoreSsoJdbcTemplate = lrcoreSsoJdbcTemplate;
    }

    /**
     * 注册新客户端。幂等：client_id 已存在则抛出异常，避免覆盖既有配置。
     *
     * @param form 注册入参
     * @return 已注册的 clientId
     */
    public String register(OAuth2ClientSaveDto form) {
        String clientId = normalizeClientId(form.getClientId());
        if (registeredClientRepository.findByClientId(clientId) != null) {
            throw new IllegalArgumentException("OAuth2 客户端 [" + clientId + "] 已存在");
        }
        RegisteredClient client = buildClient(null, clientId, form);
        registeredClientRepository.save(client);
        log.info("OAuth2 客户端 [{}] 已注册（name={}）", clientId, form.getClientName());
        return clientId;
    }

    /**
     * 更新客户端（按 clientId 定位，重建完整配置）。不存在则抛出异常。
     */
    public void update(String clientId, OAuth2ClientSaveDto form) {
        RegisteredClient existing = registeredClientRepository.findByClientId(clientId);
        if (existing == null) {
            throw new IllegalArgumentException("OAuth2 客户端 [" + clientId + "] 不存在");
        }
        registeredClientRepository.save(buildClient(existing.getId(), clientId, form));
        log.info("OAuth2 客户端 [{}] 已更新", clientId);
    }

    /**
     * 删除客户端。不存在时忽略。
     * <p>
     * 标准 {@link RegisteredClientRepository} 无删除方法，这里“尽力”通过 OAuth2
     * 持久化 {@link JdbcTemplate}（lrcoreSsoJdbcTemplate）直删
     * {@code oauth2_registered_client} 表；data-source 未装配或删除失败时记录告警。
     */
    public void delete(String clientId) {
        if (lrcoreSsoJdbcTemplate == null) {
            log.warn("OAuth2 持久化 JdbcTemplate 未装配，跳过客户端 [{}] 删除", clientId);
            return;
        }
        try {
            int rows = lrcoreSsoJdbcTemplate.update(
                    "DELETE FROM oauth2_registered_client WHERE client_id = ?", clientId);
            log.info("OAuth2 客户端 [{}] {}（{} 行）", clientId, rows > 0 ? "已删除" : "不存在", rows);
        } catch (Exception e) {
            log.error("删除 OAuth2 客户端 [{}] 失败: {}", clientId, e.getMessage());
            throw new IllegalStateException("删除 OAuth2 客户端 [" + clientId + "] 失败: " + e.getMessage());
        }
    }

    /**
     * 按 clientId 查询详情（映射为展示 DTO）。
     */
    public OAuth2ClientDto getByClientId(String clientId) {
        RegisteredClient existing = registeredClientRepository.findByClientId(clientId);
        return existing == null ? null : toDto(existing);
    }

    /**
     * 启用/禁用客户端（rebuild 后仅切换 requireAuthorizationConsent 以外的设置，
     * 通过 client_settings 中的自定义 disabled 标记存储）。
     * <p>
     * 说明：Spring Authorization Server 无原生 disabled 状态位，这里借助
     * {@code client_settings{"disabled": true/false}} 自定义项落地，并为
     * 查询/门户过滤提供依据。
     */
    public void setDisabled(String clientId, boolean disabled) {
        RegisteredClient existing = registeredClientRepository.findByClientId(clientId);
        if (existing == null) {
            throw new IllegalArgumentException("OAuth2 客户端 [" + clientId + "] 不存在");
        }
        registeredClientRepository.save(buildClient(existing.getId(), clientId, toForm(existing, disabled)));
        log.info("OAuth2 客户端 [{}] {}", clientId, disabled ? "已禁用" : "已启用");
    }

    /**
     * 尽力查询全部客户端（映射为展示 DTO 列表）。
     * 标准 Repository 无 list，data-source 未装配时返回空列表。
     */
    public List<OAuth2ClientDto> list() {
        if (lrcoreSsoJdbcTemplate == null) {
            log.warn("OAuth2 持久化 JdbcTemplate 未装配，客户端列表为空");
            return List.of();
        }
        try {
            List<Map<String, Object>> rows = lrcoreSsoJdbcTemplate.queryForList(
                    "SELECT client_id FROM oauth2_registered_client ORDER BY client_id");
            List<OAuth2ClientDto> result = new ArrayList<>();
            for (Map<String, Object> row : rows) {
                String clientId = row.get("client_id") == null ? null : String.valueOf(row.get("client_id"));
                if (clientId == null) {
                    continue;
                }
                RegisteredClient c = registeredClientRepository.findByClientId(clientId);
                if (c != null) {
                    result.add(toDto(c));
                }
            }
            return result;
        } catch (Exception e) {
            log.error("查询 OAuth2 客户端列表失败: {}", e.getMessage());
            return List.of();
        }
    }

    /* ----------------------------- 私有工具 ----------------------------- */

    private String normalizeClientId(String clientId) {
        String id = clientId == null || clientId.isBlank() ? UUID.randomUUID().toString()
                : clientId.trim();
        if (id.length() > 100) {
            throw new IllegalArgumentException("客户端 clientId 长度不能超过100个字符");
        }
        return id;
    }

    private RegisteredClient buildClient(String id, String clientId, OAuth2ClientSaveDto form) {
        boolean publicClient = form.getPublicClient() == null || form.getPublicClient();
        int accessTtl = form.getAccessTokenTtlMinutes() == null || form.getAccessTokenTtlMinutes() <= 0
                ? 30 : form.getAccessTokenTtlMinutes();
        int refreshTtl = form.getRefreshTokenTtlDays() == null || form.getRefreshTokenTtlDays() <= 0
                ? 7 : form.getRefreshTokenTtlDays();

        Set<String> redirectUris = form.getRedirectUris() == null
                ? Set.of() : new LinkedHashSet<>(form.getRedirectUris());
        if (redirectUris.isEmpty()) {
            throw new IllegalArgumentException("至少配置一个授权回调地址（redirect_uri）");
        }

        Set<String> scopes;
        if (form.getScopes() == null || form.getScopes().isEmpty()) {
            scopes = new LinkedHashSet<>(Set.of(OidcScopes.OPENID, OidcScopes.PROFILE));
        } else {
            scopes = new LinkedHashSet<>(form.getScopes());
        }

        RegisteredClient.Builder builder = RegisteredClient
                .withId(id == null ? UUID.randomUUID().toString() : id)
                .clientId(clientId)
                .clientName(form.getClientName())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUris(uris -> uris.addAll(redirectUris))
                .scopes(s -> s.addAll(scopes));
        if (form.getPostLogoutRedirectUris() != null && !form.getPostLogoutRedirectUris().isEmpty()) {
            builder.postLogoutRedirectUris(uris -> uris.addAll(form.getPostLogoutRedirectUris()));
        }
        if (publicClient) {
            // 公共 PKCE 客户端：无密钥、强制 PKCE、免授权同意页；授权码 + 刷新令牌
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .clientSettings(settings(form.getDisabled())
                            .requireProofKey(true)
                            .requireAuthorizationConsent(false)
                            .build())
                    .tokenSettings(token(accessTtl, refreshTtl));
        } else {
            // 服务端形态：必须提供密钥；授权码 + 刷新令牌；需授权同意页并强制 PKCE
            String secret = form.getClientSecret();
            if (secret == null || secret.isBlank()) {
                throw new IllegalArgumentException("服务端形态客户端必须提供 clientSecret");
            }
            builder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                    .clientSecret("{bcrypt}" + passwordEncoder.encode(secret))
                    .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                    .clientSettings(settings(form.getDisabled())
                            .requireProofKey(true)
                            .requireAuthorizationConsent(true)
                            .build())
                    .tokenSettings(token(accessTtl, refreshTtl));
        }
        return builder.build();
    }

    private ClientSettings.Builder settings(Boolean disabled) {
        ClientSettings.Builder s = ClientSettings.builder();
        if (disabled != null) {
            s.setting("disabled", disabled);
        }
        return s;
    }

    private TokenSettings token(int accessTtl, int refreshTtl) {
        return TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(accessTtl))
                .refreshTokenTimeToLive(Duration.ofDays(refreshTtl))
                .build();
    }

    private OAuth2ClientSaveDto toForm(RegisteredClient existing, boolean disabled) {
        OAuth2ClientSaveDto form = new OAuth2ClientSaveDto();
        form.setClientId(existing.getClientId())
                .setClientName(existing.getClientName())
                .setRedirectUris(existing.getRedirectUris())
                .setPostLogoutRedirectUris(existing.getPostLogoutRedirectUris())
                .setScopes(existing.getScopes())
                .setPublicClient(existing.getClientAuthenticationMethods()
                        .contains(ClientAuthenticationMethod.NONE))
                .setDisabled(disabled);
        return form;
    }

    private OAuth2ClientDto toDto(RegisteredClient c) {
        OAuth2ClientDto dto = new OAuth2ClientDto();
        dto.setClientId(c.getClientId());
        dto.setClientName(c.getClientName());
        dto.setRedirectUris(c.getRedirectUris());
        dto.setPostLogoutRedirectUris(c.getPostLogoutRedirectUris());
        dto.setScopes(c.getScopes());
        dto.setPublicClient(c.getClientAuthenticationMethods()
                .contains(ClientAuthenticationMethod.NONE));
        Object disabled = c.getClientSettings().getSetting("disabled");
        dto.setDisabled(disabled == null ? Boolean.FALSE : (Boolean) disabled);
        return dto;
    }
}