package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysTenantEntity;
import com.lrcore.system.domain.apt.SysTenantAPT;
import com.lrcore.system.service.ISysTenantService;
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

@Tag(name = "租户管理")
@RestController
@RequestMapping("/api/v1/tenants-management")
@RequiredArgsConstructor
public class SysTenantManagementController extends BaseController {

    private final ISysTenantService tenantService;

    @Operation(summary = "租户分页列表")
    @GetMapping
    public ApiResult<PageResponse<TenantItem>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) Integer status
    ) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysTenantAPT.SYS_TENANT.DELETED.eq(0));

        if (Objects.nonNull(keywords) && !keywords.isBlank()) {
            String keyword = keywords.trim();
            query.and(SysTenantAPT.SYS_TENANT.TENANT_NAME.like(keyword)
                    .or(SysTenantAPT.SYS_TENANT.TENANT_CODE.like(keyword))
                    .or(SysTenantAPT.SYS_TENANT.TENANT_DESC.like(keyword)));
        }
        if (Objects.nonNull(status)) {
            query.and(SysTenantAPT.SYS_TENANT.STATUS.eq(toBackendStatus(status)));
        }

        query.orderBy(SysTenantAPT.SYS_TENANT.SORT_ORDER.asc())
                .orderBy(SysTenantAPT.SYS_TENANT.CREATE_TIME.desc());
        Page<SysTenantEntity> result = tenantService.page(Page.of(pageNum, pageSize), query);
        List<TenantItem> items = result.getRecords().stream().map(this::toItem).toList();
        return ApiResult.success(new PageResponse<>(items, result.getTotalRow()));
    }

    @Operation(summary = "租户表单数据")
    @GetMapping("/{id}/form")
    public ApiResult<TenantForm> form(@PathVariable Long id) {
        SysTenantEntity tenant = tenantService.getById(id);
        return ApiResult.success(Objects.isNull(tenant) ? null : toForm(tenant));
    }

    @Operation(summary = "新增租户")
    @PostMapping
    public ApiResult<Boolean> create(@RequestBody TenantForm form) {
        if (existsTenantCode(form.code(), null)) {
            return ApiResult.fail("租户编码已存在");
        }
        SysTenantEntity entity = toEntity(form, null);
        entity.setSortOrder((int) (tenantService.count() + 1));
        return ApiResult.success(tenantService.save(entity));
    }

    @Operation(summary = "修改租户")
    @PutMapping("/{id}")
    public ApiResult<Boolean> update(@PathVariable Long id, @RequestBody TenantForm form) {
        if (existsTenantCode(form.code(), id)) {
            return ApiResult.fail("租户编码已存在");
        }
        return ApiResult.success(tenantService.updateById(toEntity(form, id)));
    }

    @Operation(summary = "删除租户")
    @DeleteMapping("/{ids}")
    public ApiResult<Boolean> delete(@PathVariable String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        return ApiResult.success(tenantService.removeByIds(idList));
    }

    @Operation(summary = "批量更新租户排序")
    @PutMapping("/sort")
    public ApiResult<Boolean> updateSort(@RequestBody SortRequest request) {
        return ApiResult.success(tenantService.updateSort(request.items()));
    }

    private boolean existsTenantCode(String tenantCode, Long excludeId) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysTenantAPT.SYS_TENANT.TENANT_CODE.eq(tenantCode))
                .and(SysTenantAPT.SYS_TENANT.DELETED.eq(0));
        if (Objects.nonNull(excludeId)) {
            query.and(SysTenantAPT.SYS_TENANT.ID.ne(excludeId));
        }
        return tenantService.count(query) > 0;
    }

    private TenantItem toItem(SysTenantEntity entity) {
        return new TenantItem(
                entity.getId().toString(),
                entity.getTenantName(),
                entity.getTenantCode(),
                Objects.requireNonNullElse(entity.getSortOrder(), 0),
                entity.getTenantDesc(),
                entity.getRemark(),
                toFrontendStatus(entity.getStatus())
        );
    }

    private TenantForm toForm(SysTenantEntity entity) {
        return new TenantForm(
                entity.getId().toString(),
                entity.getTenantName(),
                entity.getTenantCode(),
                entity.getTenantDesc(),
                entity.getRemark(),
                toFrontendStatus(entity.getStatus())
        );
    }

    private SysTenantEntity toEntity(TenantForm form, Long id) {
        SysTenantEntity entity = new SysTenantEntity();
        entity.setId(id);
        entity.setTenantName(form.name());
        entity.setTenantCode(form.code());
        entity.setTenantDesc(form.description());
        entity.setRemark(form.remark());
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
    private record TenantItem(
            String id,
            String name,
            String code,
            Integer sort,
            String description,
            String remark,
            Integer status
    ) {}
    private record TenantForm(
            String id,
            String name,
            String code,
            String description,
            String remark,
            Integer status
    ) {}
    private record SortRequest(List<ISysTenantService.SortItem> items) {}
}
