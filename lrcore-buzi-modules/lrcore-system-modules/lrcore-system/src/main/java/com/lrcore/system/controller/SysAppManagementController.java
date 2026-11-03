package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysAppEntity;
import com.lrcore.system.domain.apt.SysAppAPT;
import com.lrcore.system.service.ISysAppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Tag(name = "应用管理")
@RestController
@RequestMapping("/api/v1/apps")
@RequiredArgsConstructor
public class SysAppManagementController extends BaseController {

    private final ISysAppService appService;

    @Operation(summary = "应用分页列表")
    @GetMapping
    public ApiResult<PageResponse<AppItem>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Integer status
    ) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysAppAPT.SYS_APP.DELETED.eq(0));

        if (Objects.nonNull(keywords) && !keywords.isBlank()) {
            String keyword = keywords.trim();
            query.and(SysAppAPT.SYS_APP.APP_NAME.like(keyword)
                    .or(SysAppAPT.SYS_APP.APP_CODE.like(keyword))
                    .or(SysAppAPT.SYS_APP.APP_DESC.like(keyword)));
        }
        if (Objects.nonNull(status)) {
            query.and(SysAppAPT.SYS_APP.STATUS.eq(toBackendStatus(status)));
        }
        if (Objects.nonNull(tenantId)) {
            query.and(SysAppAPT.SYS_APP.TENANT_ID.eq(tenantId));
        }

        query.orderBy(SysAppAPT.SYS_APP.CREATE_TIME.desc());
        Page<SysAppEntity> result = appService.page(Page.of(pageNum, pageSize), query);
        List<AppItem> items = result.getRecords().stream().map(this::toItem).toList();
        return ApiResult.success(new PageResponse<>(items, result.getTotalRow()));
    }

    @Operation(summary = "应用表单数据")
    @GetMapping("/{id}/form")
    public ApiResult<AppForm> form(@PathVariable Long id) {
        SysAppEntity app = appService.getById(id);
        return ApiResult.success(Objects.isNull(app) ? null : toForm(app));
    }

    @Operation(summary = "新增应用")
    @PostMapping
    public ApiResult<Boolean> create(@RequestBody AppForm form) {
        if (existsAppCode(form.code(), null)) {
            return ApiResult.fail("应用编码已存在");
        }
        return ApiResult.success(appService.save(toEntity(form, null)));
    }

    @Operation(summary = "修改应用")
    @PutMapping("/{id}")
    public ApiResult<Boolean> update(@PathVariable Long id, @RequestBody AppForm form) {
        if (existsAppCode(form.code(), id)) {
            return ApiResult.fail("应用编码已存在");
        }
        return ApiResult.success(appService.updateById(toEntity(form, id)));
    }

    @Operation(summary = "删除应用")
    @DeleteMapping("/{ids}")
    public ApiResult<Boolean> delete(@PathVariable String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        return ApiResult.success(appService.removeByIds(idList));
    }

    private boolean existsAppCode(String appCode, Long excludeId) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysAppAPT.SYS_APP.APP_CODE.eq(appCode))
                .and(SysAppAPT.SYS_APP.DELETED.eq(0));
        if (Objects.nonNull(excludeId)) {
            query.and(SysAppAPT.SYS_APP.ID.ne(excludeId));
        }
        return appService.count(query) > 0;
    }

    private AppItem toItem(SysAppEntity entity) {
        return new AppItem(
                entity.getId().toString(),
                entity.getAppName(),
                entity.getAppCode(),
                entity.getAppDesc(),
                entity.getAppUrl(),
                entity.getAppIcon(),
                entity.getPermissionPrefix(),
                entity.getRemark(),
                entity.getTenantId(),
                toFrontendStatus(entity.getStatus())
        );
    }

    private AppForm toForm(SysAppEntity entity) {
        return new AppForm(
                entity.getId().toString(),
                entity.getAppName(),
                entity.getAppCode(),
                entity.getAppDesc(),
                entity.getAppUrl(),
                entity.getAppIcon(),
                entity.getPermissionPrefix(),
                entity.getRemark(),
                entity.getTenantId(),
                toFrontendStatus(entity.getStatus())
        );
    }

    private SysAppEntity toEntity(AppForm form, Long id) {
        SysAppEntity entity = new SysAppEntity();
        entity.setId(id);
        entity.setAppName(form.name());
        entity.setAppCode(form.code());
        entity.setAppDesc(form.description());
        entity.setAppUrl(form.url());
        entity.setAppIcon(form.icon());
        entity.setPermissionPrefix(form.permissionPrefix());
        entity.setRemark(form.remark());
        entity.setTenantId(form.tenantId());
        entity.setStatus(toBackendStatus(form.status()));
        entity.setDeleted(0);
        return entity;
    }

    private Integer toBackendStatus(Integer status) {
        return Objects.equals(status, 1) ? 0 : 1;
    }

    private Integer toFrontendStatus(Integer status) {
        return Objects.equals(status, 0) ? 1 : 0;
    }

    private record PageResponse<T>(List<T> list, long total) {}
    private record AppItem(
            String id,
            String name,
            String code,
            String description,
            String url,
            String icon,
            String permissionPrefix,
            String remark,
            Long tenantId,
            Integer status
    ) {}
    private record AppForm(
            String id,
            String name,
            String code,
            String description,
            String url,
            String icon,
            String permissionPrefix,
            String remark,
            Long tenantId,
            Integer status
    ) {}
}
