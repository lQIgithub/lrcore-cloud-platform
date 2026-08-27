package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.api.RemoteOAuth2ClientApi;
import com.lrcore.system.api.dto.OAuth2ClientDto;
import com.lrcore.system.api.dto.OAuth2ClientSaveDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: OAuth2 客户端管理控制器（后台管理系统对外提供，供管理员维护平台的
 *            OAuth2 已注册客户端）。
 * <p>
 * 请求链路（安全边界）：
 * <pre>
 * 管理员浏览器(网关 SSO JWT 鉴权)
 *   → GET/POST/DELETE /oauth2Client/...  本控制器
 *   → {@link RemoteOAuth2ClientApi}（Feign，自动带 from-source: inner）
 *   → lrcore-auth /api/v1/client/admin/**（@InnerAuth 校验）
 * </pre>
 * 本控制器本身拦在网关之后，需携带合法 SAS 访问令牌才能到达；真正增改 OAuth2 客户端
 * 的实现细节在认证中心内部完成，本模块与认证中心通过 Feign 解耦，互不越权。
 * @ClassName: OAuth2ClientController
 * @Author: lrcore
 * @Date 2026/8/27
 * @Version 1.0
 */
@RestController
@RequestMapping("/oauth2Client")
@RequiredArgsConstructor
@Schema(description = "OAuth2 客户端管理控制器")
public class OAuth2ClientController extends BaseController {

    private final RemoteOAuth2ClientApi remoteOAuth2ClientApi;

    @PostMapping("/register")
    @Schema(description = "注册 OAuth2 客户端")
    public ApiResult<String> register(@RequestBody OAuth2ClientSaveDto form) {
        return remoteOAuth2ClientApi.register(form);
    }

    @PutMapping("/{clientId}")
    @Schema(description = "更新 OAuth2 客户端")
    public ApiResult<Boolean> update(@PathVariable String clientId, @RequestBody OAuth2ClientSaveDto form) {
        return remoteOAuth2ClientApi.update(clientId, form);
    }

    @DeleteMapping("/{clientId}")
    @Schema(description = "删除 OAuth2 客户端")
    public ApiResult<Boolean> delete(@PathVariable String clientId) {
        return remoteOAuth2ClientApi.delete(clientId);
    }

    @GetMapping("/list")
    @Schema(description = "查询 OAuth2 客户端列表")
    public ApiResult<List<OAuth2ClientDto>> list() {
        return remoteOAuth2ClientApi.list();
    }

    @GetMapping("/{clientId}")
    @Schema(description = "按 clientId 查询 OAuth2 客户端详情")
    public ApiResult<OAuth2ClientDto> detail(@PathVariable String clientId) {
        return remoteOAuth2ClientApi.detail(clientId);
    }

    @PostMapping("/{clientId}/enable")
    @Schema(description = "启用 OAuth2 客户端")
    public ApiResult<Boolean> enable(@PathVariable String clientId) {
        return remoteOAuth2ClientApi.enable(clientId);
    }

    @PostMapping("/{clientId}/disable")
    @Schema(description = "禁用 OAuth2 客户端")
    public ApiResult<Boolean> disable(@PathVariable String clientId) {
        return remoteOAuth2ClientApi.disable(clientId);
    }
}