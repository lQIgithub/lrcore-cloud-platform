package com.lrcore.system.feign;

import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.api.RemoteSysAppApi;
import com.lrcore.system.api.dto.PortalAppDto;
import com.lrcore.system.domain.SysAppEntity;
import com.lrcore.system.service.ISysAppService;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 应用系统（子系统）门户清单 —— Feign 远程接口实现。
 *            认证中心（lrcore-auth）单点登录成功后加载子门户页列表即调用本端点。
 * @ClassName: SysAppPortalController
 * @Author: lrcore
 * @Date 2026/8/21
 * @Version 1.0
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SysAppPortalController implements RemoteSysAppApi {

    private final ISysAppService sysAppService;

    @Override
    public ApiResult<List<PortalAppDto>> portalList() {
        List<SysAppEntity> apps = sysAppService.list(QueryWrapper.create()
                .where(SysAppEntity::getStatus).eq(0)
                .and(SysAppEntity::getDeleted).eq(0));
        List<PortalAppDto> portalApps = apps.stream().map(this::toPortal).toList();
        log.info("门户子系统清单查询完成, 共 {} 个", portalApps.size());
        return ApiResult.success(portalApps);
    }

    private PortalAppDto toPortal(SysAppEntity entity) {
        PortalAppDto dto = new PortalAppDto();
        dto.setAppCode(entity.getAppCode());
        dto.setAppName(entity.getAppName());
        dto.setAppDesc(entity.getAppDesc());
        dto.setAppUrl(entity.getAppUrl());
        dto.setAppIcon(entity.getAppIcon());
        return dto;
    }
}