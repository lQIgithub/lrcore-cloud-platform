package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysConfigEntity;
import com.lrcore.system.domain.apt.SysConfigAPT;
import com.lrcore.system.service.ISysConfigService;
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

import java.util.List;
import java.util.Objects;

@Tag(name = "系统参数配置")
@RestController
@RequestMapping("/api/v1/configs")
@RequiredArgsConstructor
@Schema(description = "参数配置 控制器")
public class SysConfigController extends BaseController {

    private final ISysConfigService configService;

    @Operation(summary = "系统配置分页列表")
    @GetMapping
    public ApiResult<PageResponse<ConfigItem>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keywords
    ) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysConfigAPT.SYS_CONFIG.DELETED.eq(0));

        if (Objects.nonNull(keywords) && !keywords.isBlank()) {
            String keyword = keywords.trim();
            query.and(SysConfigAPT.SYS_CONFIG.CONFIG_NAME.like(keyword)
                    .or(SysConfigAPT.SYS_CONFIG.CONFIG_KEY.like(keyword)));
        }

        query.orderBy(SysConfigAPT.SYS_CONFIG.CREATE_TIME.desc());
        Page<SysConfigEntity> result = configService.page(Page.of(pageNum, pageSize), query);
        List<ConfigItem> items = result.getRecords().stream().map(this::toItem).toList();
        return ApiResult.success(new PageResponse<>(items, result.getTotalRow()));
    }

    @Operation(summary = "根据配置键获取配置")
    @GetMapping("/key/{configKey}")
    public ApiResult<SysConfigEntity> getByKey(@PathVariable String configKey) {
        return ApiResult.success(configService.getByConfigKey(configKey));
    }

    @Operation(summary = "系统配置详情")
    @GetMapping("/{id}/form")
    public ApiResult<ConfigForm> form(@PathVariable Long id) {
        SysConfigEntity config = configService.getById(id);
        return ApiResult.success(Objects.isNull(config) ? null : toForm(config));
    }

    @Operation(summary = "新增系统配置")
    @PostMapping
    public ApiResult<Boolean> create(@RequestBody ConfigForm form) {
        if (existsConfigKey(form.configKey(), null)) {
            return ApiResult.fail("配置键已存在");
        }

        SysConfigEntity entity = toEntity(form, null);
        entity.setConfigType("N");
        entity.setDeleted(0);
        return ApiResult.success(configService.save(entity));
    }

    @Operation(summary = "修改系统配置")
    @PutMapping("/{id}")
    public ApiResult<Boolean> update(@PathVariable Long id, @RequestBody ConfigForm form) {
        if (existsConfigKey(form.configKey(), id)) {
            return ApiResult.fail("配置键已存在");
        }
        return ApiResult.success(configService.updateById(toEntity(form, id)));
    }

    @Operation(summary = "删除系统配置")
    @DeleteMapping("/{id}")
    public ApiResult<Boolean> delete(@PathVariable Long id) {
        SysConfigEntity config = configService.getById(id);
        if (Objects.isNull(config)) {
            return ApiResult.success(false);
        }
        if (Objects.equals("Y", config.getConfigType())) {
            return ApiResult.fail("系统内置配置不允许删除");
        }
        return ApiResult.success(configService.removeById(config));
    }

    @Operation(summary = "刷新系统配置缓存")
    @PutMapping("/refresh")
    public ApiResult<Boolean> refreshCache() {
        return ApiResult.success(configService.refreshCache());
    }

    private boolean existsConfigKey(String configKey, Long excludeId) {
        QueryWrapper query = QueryWrapper.create()
                .where(SysConfigAPT.SYS_CONFIG.CONFIG_KEY.eq(configKey))
                .and(SysConfigAPT.SYS_CONFIG.DELETED.eq(0));
        if (Objects.nonNull(excludeId)) {
            query.and(SysConfigAPT.SYS_CONFIG.ID.ne(excludeId));
        }
        return configService.count(query) > 0;
    }

    private ConfigItem toItem(SysConfigEntity entity) {
        return new ConfigItem(
                entity.getId().toString(),
                entity.getConfigName(),
                entity.getConfigKey(),
                entity.getConfigValue(),
                entity.getRemark()
        );
    }

    private ConfigForm toForm(SysConfigEntity entity) {
        return new ConfigForm(
                entity.getId().toString(),
                entity.getConfigName(),
                entity.getConfigKey(),
                entity.getConfigValue(),
                entity.getRemark()
        );
    }

    private SysConfigEntity toEntity(ConfigForm form, Long id) {
        SysConfigEntity entity = new SysConfigEntity();
        entity.setId(id);
        entity.setConfigName(form.configName());
        entity.setConfigKey(form.configKey());
        entity.setConfigValue(form.configValue());
        entity.setRemark(form.remark());
        return entity;
    }

    private record PageResponse<T>(List<T> list, long total) {}
    private record ConfigItem(
            String id,
            String configName,
            String configKey,
            String configValue,
            String remark
    ) {}
    private record ConfigForm(
            String id,
            String configName,
            String configKey,
            String configValue,
            String remark
    ) {}
}
