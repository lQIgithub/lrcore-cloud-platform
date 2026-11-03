package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysDictDataEntity;
import com.lrcore.system.domain.apt.SysDictDataAPT;
import com.lrcore.system.service.ISysDictDataService;
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

@Tag(name = "数据字典信息")
@RestController
@RequestMapping("/api/v1/dicts")
@RequiredArgsConstructor
@Schema(description = "数据字典信息 控制器")
public class SysDictDataController extends BaseController {

    private final ISysDictDataService dictDataService;

    @Operation(summary = "字典数据分页列表")
    @GetMapping("/{dictCode}/items")
    public ApiResult<PageResponse<DictDataItem>> page(
            @PathVariable String dictCode,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keywords
    ) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysDictDataAPT.SYS_DICT_DATA.DELETED.eq(0))
                .and(SysDictDataAPT.SYS_DICT_DATA.DICT_TYPE.eq(dictCode));

        if (Objects.nonNull(keywords) && !keywords.isBlank()) {
            String keyword = keywords.trim();
            query.and(SysDictDataAPT.SYS_DICT_DATA.DICT_LABEL.like(keyword)
                    .or(SysDictDataAPT.SYS_DICT_DATA.DICT_VALUE.like(keyword)));
        }

        query.orderBy(SysDictDataAPT.SYS_DICT_DATA.DICT_SORT.asc());
        Page<SysDictDataEntity> result = dictDataService.page(Page.of(pageNum, pageSize), query);
        List<DictDataItem> items = result.getRecords().stream().map(this::toItem).toList();
        return ApiResult.success(new PageResponse<>(items, result.getTotalRow()));
    }

    @Operation(summary = "字典数据下拉选项")
    @GetMapping("/{dictCode}/items/options")
    public ApiResult<List<DictDataOption>> options(@PathVariable String dictCode) {
        List<SysDictDataEntity> dataList = dictDataService.list(QueryWrapper.create()
                .where(SysDictDataAPT.SYS_DICT_DATA.DELETED.eq(0))
                .and(SysDictDataAPT.SYS_DICT_DATA.DICT_TYPE.eq(dictCode))
                .and(SysDictDataAPT.SYS_DICT_DATA.STATUS.eq(0))
                .orderBy(SysDictDataAPT.SYS_DICT_DATA.DICT_SORT.asc()));
        return ApiResult.success(dataList.stream()
                .map(item -> new DictDataOption(
                        item.getDictValue(),
                        item.getDictLabel(),
                        item.getListClass()
                ))
                .toList());
    }

    @Operation(summary = "字典数据详情")
    @GetMapping("/{dictCode}/items/{id}/form")
    public ApiResult<DictDataForm> form(
            @PathVariable String dictCode,
            @PathVariable Long id
    ) {
        SysDictDataEntity data = dictDataService.getById(id);
        if (Objects.isNull(data) || !Objects.equals(data.getDictType(), dictCode)) {
            return ApiResult.success(null);
        }
        return ApiResult.success(toForm(data));
    }

    @Operation(summary = "新增字典数据")
    @PostMapping("/{dictCode}/items")
    public ApiResult<Boolean> create(
            @PathVariable String dictCode,
            @RequestBody DictDataForm form
    ) {
        SysDictDataEntity entity = toEntity(dictCode, form, null);
        entity.setDeleted(0);
        return ApiResult.success(dictDataService.save(entity));
    }

    @Operation(summary = "修改字典数据")
    @PutMapping("/{dictCode}/items/{id}")
    public ApiResult<Boolean> update(
            @PathVariable String dictCode,
            @PathVariable Long id,
            @RequestBody DictDataForm form
    ) {
        return ApiResult.success(dictDataService.updateById(toEntity(dictCode, form, id)));
    }

    @Operation(summary = "删除字典数据")
    @DeleteMapping("/{dictCode}/items/{ids}")
    public ApiResult<Boolean> delete(@PathVariable String ids) {
        List<Long> idList = Arrays.stream(ids.split(","))
                .filter(id -> !id.isBlank())
                .map(Long::valueOf)
                .toList();
        return ApiResult.success(dictDataService.removeByIds(idList));
    }

    @Operation(summary = "批量更新字典数据排序")
    @PutMapping("/{dictCode}/items/sort")
    public ApiResult<Boolean> updateSort(
            @PathVariable String dictCode,
            @RequestBody SortRequest request
    ) {
        return ApiResult.success(dictDataService.updateSort(dictCode, request.items()));
    }

    private DictDataItem toItem(SysDictDataEntity entity) {
        return new DictDataItem(
                entity.getId().toString(),
                entity.getDictType(),
                entity.getDictLabel(),
                entity.getDictValue(),
                toFrontendStatus(entity.getStatus()),
                parseSort(entity.getDictSort()),
                entity.getListClass()
        );
    }

    private DictDataForm toForm(SysDictDataEntity entity) {
        return new DictDataForm(
                entity.getId().toString(),
                entity.getDictType(),
                entity.getDictLabel(),
                entity.getDictValue(),
                toFrontendStatus(entity.getStatus()),
                parseSort(entity.getDictSort()),
                entity.getListClass()
        );
    }

    private SysDictDataEntity toEntity(String dictCode, DictDataForm form, Long id) {
        SysDictDataEntity entity = new SysDictDataEntity();
        entity.setId(id);
        entity.setDictCode(dictCode);
        entity.setDictType(dictCode);
        entity.setDictLabel(form.label());
        entity.setDictValue(form.value());
        entity.setStatus(toBackendStatus(form.status()));
        entity.setDictSort(Objects.toString(form.sort(), "0"));
        entity.setCssClass(form.tagType());
        entity.setListClass(form.tagType());
        return entity;
    }

    private Integer parseSort(String sort) {
        try {
            return Integer.valueOf(sort);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private Integer toBackendStatus(Integer status) {
        return Objects.equals(status, 1) ? 0 : 1;
    }

    private Integer toFrontendStatus(Integer status) {
        return Objects.equals(status, 0) ? 1 : 0;
    }

    private record PageResponse<T>(List<T> list, long total) {}
    private record DictDataItem(
            String id,
            String dictCode,
            String label,
            String value,
            Integer status,
            Integer sort,
            String tagType
    ) {}
    private record DictDataForm(
            String id,
            String dictCode,
            String label,
            String value,
            Integer status,
            Integer sort,
            String tagType
    ) {}
    private record DictDataOption(String value, String label, String tagType) {}
    private record SortRequest(List<ISysDictDataService.SortItem> items) {}
}
