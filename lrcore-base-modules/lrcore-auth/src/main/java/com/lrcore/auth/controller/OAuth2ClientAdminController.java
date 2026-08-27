package com.lrcore.auth.controller;

import com.lrcore.auth.service.OAuth2ClientAdminService;
import com.lrcore.common.annotations.annotation.auth.InnerAuth;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.api.dto.OAuth2ClientDto;
import com.lrcore.system.api.dto.OAuth2ClientSaveDto;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: OAuth2 客户端管理接口（平台内部专用，仅供后台管理系统经 Feign 调用）。
 * <p>
 * 安全边界：
 * <ul>
 *   <li>本控制器所有端点均标注 {@link InnerAuth}：只接受携带
 *       {@code from-source: inner} 请求头的内部服务调用（由
 *       lrcore-common-feign 的请求拦截器在发起 Feign 调用时自动注入），
 *       外部 HTTP 直连一律被 {@code InnerAuthAspect} 拒绝；</li>
 *   <li>对外管理员入口在 lrcore-system（走网关 SSO JWT 鉴权 + 权限校验），
 *       本模块仅作为服务端实现承接内部调用，二者经 Feign 解耦。</li>
 * </ul>
 * 路由：{@code /api/v1/client/admin/**}（已加入 {@code LrcoreSsoSecurityConfig}
 * 的 {@code PUBLIC_PATHS}，因内部 Feign 直连本服务不经网关，需在宿主安全链放行，
 * 真正的鉴权由 {@code @InnerAuth} 承担）。
 * @ClassName: OAuth2ClientAdminController
 * @Author: lrcore
 * @Date: 2026/8/27
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/client/admin")
@RequiredArgsConstructor
@Schema(description = "OAuth2 客户端管理接口（内部专用）")
public class OAuth2ClientAdminController extends BaseController {

    private final OAuth2ClientAdminService oauth2ClientAdminService;

    @InnerAuth
    @PostMapping("/register")
    @Schema(description = "注册 OAuth2 客户端")
    public ApiResult<String> register(@Valid @RequestBody OAuth2ClientSaveDto form) {
        return ApiResult.success(oauth2ClientAdminService.register(form));
    }

    @InnerAuth
    @PutMapping("/{clientId}")
    @Schema(description = "更新 OAuth2 客户端")
    public ApiResult<Boolean> update(
            @Parameter(description = "客户端 clientId", required = true) @PathVariable String clientId,
            @Valid @RequestBody OAuth2ClientSaveDto form) {
        oauth2ClientAdminService.update(clientId, form);
        return ApiResult.success(true);
    }

    @InnerAuth
    @DeleteMapping("/{clientId}")
    @Schema(description = "删除 OAuth2 客户端")
    public ApiResult<Boolean> delete(
            @Parameter(description = "客户端 clientId", required = true) @PathVariable String clientId) {
        oauth2ClientAdminService.delete(clientId);
        return ApiResult.success(true);
    }

    @InnerAuth
    @GetMapping("/{clientId}")
    @Schema(description = "按 clientId 查询 OAuth2 客户端详情")
    public ApiResult<OAuth2ClientDto> detail(
            @Parameter(description = "客户端 clientId", required = true) @PathVariable String clientId) {
        return ApiResult.success(oauth2ClientAdminService.getByClientId(clientId));
    }

    @InnerAuth
    @PostMapping("/{clientId}/enable")
    @Schema(description = "启用 OAuth2 客户端")
    public ApiResult<Boolean> enable(
            @Parameter(description = "客户端 clientId", required = true) @PathVariable String clientId) {
        oauth2ClientAdminService.setDisabled(clientId, false);
        return ApiResult.success(true);
    }

    @InnerAuth
    @PostMapping("/{clientId}/disable")
    @Schema(description = "禁用 OAuth2 客户端")
    public ApiResult<Boolean> disable(
            @Parameter(description = "客户端 clientId", required = true) @PathVariable String clientId) {
        oauth2ClientAdminService.setDisabled(clientId, true);
        return ApiResult.success(true);
    }

    @InnerAuth
    @GetMapping("/list")
    @Schema(description = "查询全部 OAuth2 客户端")
    public ApiResult<List<OAuth2ClientDto>> list() {
        return ApiResult.success(oauth2ClientAdminService.list());
    }
}