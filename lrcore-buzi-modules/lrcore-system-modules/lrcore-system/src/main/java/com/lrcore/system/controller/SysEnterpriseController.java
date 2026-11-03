package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysEnterpriseEntity;
import com.lrcore.system.domain.apt.SysEnterpriseAPT;
import com.lrcore.system.service.ISysEnterpriseService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
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
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Tag(name = "企业管理")
@RestController
@RequestMapping("/api/v1/enterprises")
@RequiredArgsConstructor
@Schema(description = "企业管理 控制器")
public class SysEnterpriseController extends BaseController {

    private final ISysEnterpriseService enterpriseService;

    @Operation(summary = "企业分页列表")
    @GetMapping
    public ApiResult<PageResponse<EnterpriseItem>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Integer status
    ) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysEnterpriseAPT.SYS_ENTERPRISE.DELETED.eq(0));

        if (Objects.nonNull(keywords) && !keywords.isBlank()) {
            String keyword = keywords.trim();
            query.and(SysEnterpriseAPT.SYS_ENTERPRISE.ENT_NAME.like(keyword)
                    .or(SysEnterpriseAPT.SYS_ENTERPRISE.ENT_CODE.like(keyword))
                    .or(SysEnterpriseAPT.SYS_ENTERPRISE.ENT_ABBREVIATION.like(keyword)));
        }
        if (Objects.nonNull(status)) {
            query.and(SysEnterpriseAPT.SYS_ENTERPRISE.STATUS.eq(toBackendStatus(status)));
        }
        if (Objects.nonNull(tenantId)) {
            query.and(SysEnterpriseAPT.SYS_ENTERPRISE.TENANT_ID.eq(tenantId));
        }

        query.orderBy(SysEnterpriseAPT.SYS_ENTERPRISE.CREATE_TIME.desc());
        Page<SysEnterpriseEntity> result = enterpriseService.page(Page.of(pageNum, pageSize), query);
        Map<Long, String> parentNames = getParentNames(result.getRecords());
        List<EnterpriseItem> items = result.getRecords().stream()
                .map(item -> toItem(item, parentNames))
                .toList();
        return ApiResult.success(new PageResponse<>(items, result.getTotalRow()));
    }

    @Operation(summary = "企业表单数据")
    @GetMapping("/{id}/form")
    public ApiResult<EnterpriseForm> form(@PathVariable Long id) {
        SysEnterpriseEntity enterprise = enterpriseService.getById(id);
        return ApiResult.success(Objects.isNull(enterprise) ? null : toForm(enterprise));
    }

    @Operation(summary = "上级企业下拉选项")
    @GetMapping("/options")
    public ApiResult<List<Option>> options() {
        List<SysEnterpriseEntity> enterprises = enterpriseService.list(QueryWrapper.create()
                .where(SysEnterpriseAPT.SYS_ENTERPRISE.DELETED.eq(0))
                .orderBy(SysEnterpriseAPT.SYS_ENTERPRISE.ENT_NAME.asc()));
        return ApiResult.success(enterprises.stream()
                .map(item -> new Option(
                        item.getId().toString(),
                        item.getEntName(),
                        item.getEntCode(),
                        item.getEntAbbreviation()
                ))
                .toList());
    }

    @Operation(summary = "新增企业")
    @PostMapping
    public ApiResult<Boolean> create(@RequestBody EnterpriseForm form) {
        if (existsEntCode(form.code(), null)) {
            return ApiResult.fail("企业编码已存在");
        }
        SysEnterpriseEntity entity = toEntity(form, null);
        entity.setDeleted(0);
        return ApiResult.success(enterpriseService.save(entity));
    }

    @Operation(summary = "修改企业")
    @PutMapping("/{id}")
    public ApiResult<Boolean> update(@PathVariable Long id, @RequestBody EnterpriseForm form) {
        if (existsEntCode(form.code(), id)) {
            return ApiResult.fail("企业编码已存在");
        }
        return ApiResult.success(enterpriseService.updateById(toEntity(form, id)));
    }

    @Operation(summary = "删除企业")
    @DeleteMapping("/{ids}")
    public ApiResult<Boolean> delete(@PathVariable String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .filter(value -> !value.isBlank())
                .map(Long::valueOf)
                .toList();
        return ApiResult.success(enterpriseService.removeByIds(idList));
    }

    private boolean existsEntCode(String entCode, Long excludeId) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysEnterpriseAPT.SYS_ENTERPRISE.ENT_CODE.eq(entCode))
                .and(SysEnterpriseAPT.SYS_ENTERPRISE.DELETED.eq(0));
        if (Objects.nonNull(excludeId)) {
            query.and(SysEnterpriseAPT.SYS_ENTERPRISE.ID.ne(excludeId));
        }
        return enterpriseService.count(query) > 0;
    }

    private Map<Long, String> getParentNames(List<SysEnterpriseEntity> enterprises) {
        List<Long> parentIds = enterprises.stream()
                .map(SysEnterpriseEntity::getParentId)
                .filter(Objects::nonNull)
                .filter(parentId -> parentId != 0L)
                .distinct()
                .toList();
        if (parentIds.isEmpty()) {
            return Map.of();
        }
        return enterpriseService.list(QueryWrapper.create()
                        .where(SysEnterpriseAPT.SYS_ENTERPRISE.DELETED.eq(0))
                        .and(SysEnterpriseAPT.SYS_ENTERPRISE.ID.in(parentIds)))
                .stream()
                .collect(Collectors.toMap(SysEnterpriseEntity::getId, SysEnterpriseEntity::getEntName));
    }

    private EnterpriseItem toItem(SysEnterpriseEntity entity, Map<Long, String> parentNames) {
        return new EnterpriseItem(
                entity.getId().toString(),
                entity.getEntName(),
                entity.getEntAbbreviation(),
                entity.getEntCode(),
                entity.getParentId(),
                Objects.isNull(entity.getParentId()) || entity.getParentId() == 0L
                        ? "顶级企业" : parentNames.getOrDefault(entity.getParentId(), ""),
                entity.getPhone(),
                entity.getAddress(),
                entity.getEntDesc(),
                entity.getTenantId(),
                toFrontendStatus(entity.getStatus())
        );
    }

    private EnterpriseForm toForm(SysEnterpriseEntity entity) {
        return new EnterpriseForm(
                entity.getId().toString(),
                entity.getEntName(),
                entity.getEntAbbreviation(),
                entity.getEntCode(),
                entity.getParentId(),
                entity.getEntDesc(),
                entity.getPhone(),
                entity.getAddress(),
                entity.getTenantId(),
                toFrontendStatus(entity.getStatus())
        );
    }

    private SysEnterpriseEntity toEntity(EnterpriseForm form, Long id) {
        SysEnterpriseEntity entity = new SysEnterpriseEntity();
        entity.setId(id);
        entity.setEntName(form.name());
        entity.setEntAbbreviation(form.abbreviation());
        entity.setEntCode(form.code());
        entity.setParentId(form.parentId());
        entity.setEntDesc(form.description());
        entity.setPhone(form.phone());
        entity.setAddress(form.address());
        entity.setTenantId(form.tenantId());
        entity.setStatus(toBackendStatus(form.status()));
        entity.setAncestors(resolveAncestors(form.parentId()));
        return entity;
    }

    private String resolveAncestors(Long parentId) {
        if (Objects.isNull(parentId) || parentId == 0L) {
            return "0";
        }
        SysEnterpriseEntity parent = enterpriseService.getById(parentId);
        if (Objects.isNull(parent)) {
            return String.valueOf(parentId);
        }
        String parentAncestors = Objects.toString(parent.getAncestors(), "");
        if (parentAncestors.isBlank() || Objects.equals(parent.getParentId(), 0L)) {
            return String.valueOf(parentId);
        }
        return parentAncestors + "," + parentId;
    }

    private Integer toBackendStatus(Integer status) {
        return Objects.equals(status, 1) ? 0 : 1;
    }

    private Integer toFrontendStatus(Integer status) {
        return Objects.equals(status, 0) ? 1 : 0;
    }

    private record PageResponse<T>(List<T> list, long total) {}
    private record Option(String value, String label, String code, String abbreviation) {}
    private record EnterpriseItem(
            String id,
            String name,
            String abbreviation,
            String code,
            Long parentId,
            String parentName,
            String phone,
            String address,
            String description,
            Long tenantId,
            Integer status
    ) {}
    private record EnterpriseForm(
            String id,
            String name,
            String abbreviation,
            String code,
            Long parentId,
            String description,
            String phone,
            String address,
            Long tenantId,
            Integer status
    ) {}
}
