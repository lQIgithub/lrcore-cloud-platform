package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysDictTypeEntity;
import com.lrcore.system.domain.SysAppEntity;
import com.lrcore.system.domain.apt.SysDictTypeAPT;
import com.lrcore.system.service.ISysAppService;
import com.lrcore.system.service.ISysDictTypeService;
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
import java.util.Objects;

@Tag(name = "数据字典类型")
@RestController
@RequestMapping("/api/v1/dicts")
@RequiredArgsConstructor
@Schema(description = "数据字典类型 控制器")
public class SysDictTypeController extends BaseController {

    private final ISysDictTypeService dictTypeService;
    private final ISysAppService appService;

    @Operation(summary = "字典类型分页列表")
    @GetMapping
    public ApiResult<PageResponse<DictTypeItem>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long appId
    ) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysDictTypeAPT.SYS_DICT_TYPE.DELETED.eq(0));

        if (Objects.nonNull(keywords) && !keywords.isBlank()) {
            String keyword = keywords.trim();
            query.and(SysDictTypeAPT.SYS_DICT_TYPE.DICT_NAME.like(keyword)
                    .or(SysDictTypeAPT.SYS_DICT_TYPE.DICT_TYPE.like(keyword)));
        }
        if (Objects.nonNull(status)) {
            query.and(SysDictTypeAPT.SYS_DICT_TYPE.STATUS.eq(toBackendStatus(status)));
        }
        if (Objects.nonNull(appId)) {
            query.and(SysDictTypeAPT.SYS_DICT_TYPE.APP_ID.eq(appId));
        }

        query.orderBy(SysDictTypeAPT.SYS_DICT_TYPE.CREATE_TIME.desc());
        Page<SysDictTypeEntity> result = dictTypeService.page(Page.of(pageNum, pageSize), query);
        List<DictTypeItem> items = result.getRecords().stream().map(this::toItem).toList();
        return ApiResult.success(new PageResponse<>(items, result.getTotalRow()));
    }

    @Operation(summary = "字典类型下拉选项")
    @GetMapping("/options")
    public ApiResult<List<Option>> options() {
        List<SysDictTypeEntity> types = dictTypeService.list(QueryWrapper.create()
                .where(SysDictTypeAPT.SYS_DICT_TYPE.DELETED.eq(0))
                .and(SysDictTypeAPT.SYS_DICT_TYPE.STATUS.eq(0))
                .orderBy(SysDictTypeAPT.SYS_DICT_TYPE.DICT_NAME.asc()));
        return ApiResult.success(types.stream()
                .map(type -> new Option(type.getDictType(), type.getDictName(), type.getDictType()))
                .toList());
    }

    @Operation(summary = "字典类型详情")
    @GetMapping("/{id}/form")
    public ApiResult<DictTypeForm> form(@PathVariable Long id) {
        SysDictTypeEntity type = dictTypeService.getById(id);
        return ApiResult.success(Objects.isNull(type) ? null : toForm(type));
    }

    @Operation(summary = "新增字典类型")
    @PostMapping
    public ApiResult<Boolean> create(@RequestBody DictTypeForm form) {
        Long appId = parseAppId(form.appId());
        Long tenantId = validateAppRelation(appId, form.tenantId());
        SysDictTypeEntity entity = toEntity(form, null, tenantId);
        entity.setDeleted(0);
        return ApiResult.success(dictTypeService.save(entity));
    }

    @Operation(summary = "修改字典类型")
    @PutMapping("/{id}")
    public ApiResult<Boolean> update(@PathVariable Long id, @RequestBody DictTypeForm form) {
        Long appId = parseAppId(form.appId());
        Long tenantId = validateAppRelation(appId, form.tenantId());
        SysDictTypeEntity entity = toEntity(form, id, tenantId);
        return ApiResult.success(dictTypeService.updateById(entity));
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/{ids}")
    public ApiResult<Boolean> delete(@PathVariable String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        return ApiResult.success(dictTypeService.removeByIds(idList));
    }

    private DictTypeItem toItem(SysDictTypeEntity entity) {
        return new DictTypeItem(
                entity.getId().toString(),
                entity.getDictName(),
                entity.getDictType(),
                Objects.toString(entity.getAppId(), "0"),
                toFrontendStatus(entity.getStatus())
        );
    }

    private DictTypeForm toForm(SysDictTypeEntity entity) {
        return new DictTypeForm(
                entity.getId().toString(),
                entity.getDictName(),
                entity.getDictType(),
                Objects.toString(entity.getAppId(), "0"),
                Objects.toString(entity.getTenantId(), null),
                toFrontendStatus(entity.getStatus()),
                entity.getRemark()
        );
    }

    private SysDictTypeEntity toEntity(DictTypeForm form, Long id, Long tenantId) {
        SysDictTypeEntity entity = new SysDictTypeEntity();
        entity.setId(id);
        entity.setDictName(form.name());
        entity.setDictType(form.dictCode());
        entity.setAppId(parseAppId(form.appId()));
        entity.setTenantId(tenantId);
        entity.setStatus(toBackendStatus(form.status()));
        entity.setRemark(form.remark());
        return entity;
    }

    private Long parseAppId(String appId) {
        if (Objects.isNull(appId) || appId.isBlank()) {
            return 0L;
        }
        try {
            return Long.valueOf(appId);
        } catch (NumberFormatException ignored) {
            throw new ServiceException("应用ID格式不正确");
        }
    }

    private Long validateAppRelation(Long appId, String requestTenantId) {
        if (Objects.isNull(appId) || appId <= 0) {
            throw new ServiceException("应用ID不能为空");
        }
        SysAppEntity app = appService.getById(appId);
        if (Objects.isNull(app)) {
            throw new ServiceException("应用不存在");
        }
        if (Objects.nonNull(app.getTenantId())
                && Objects.nonNull(requestTenantId)
                && !requestTenantId.isBlank()
                && !Objects.equals(app.getTenantId(), Long.valueOf(requestTenantId))) {
            throw new ServiceException("应用与租户不匹配");
        }
        return app.getTenantId();
    }

    private Integer toBackendStatus(Integer status) {
        return Objects.equals(status, 1) ? 0 : 1;
    }

    private Integer toFrontendStatus(Integer status) {
        return Objects.equals(status, 0) ? 1 : 0;
    }

    private record PageResponse<T>(List<T> list, long total) {}
    private record DictTypeItem(String id, String name, String dictCode, String appId, Integer status) {}
    private record DictTypeForm(
            String id,
            String name,
            String dictCode,
            String appId,
            String tenantId,
            Integer status,
            String remark
    ) {}
    private record Option(String value, String label, String code) {}
}
