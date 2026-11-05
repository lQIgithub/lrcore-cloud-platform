package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.lrcore.system.domain.SysAppEntity;
import com.lrcore.system.domain.SysDataPermissionRuleEntity;
import com.lrcore.system.service.ISysAppService;
import com.lrcore.system.service.ISysDataPermissionRuleService;
import io.swagger.v3.oas.annotations.media.Schema;
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

import java.io.Serializable;
import java.util.List;

/**
 * 数据权限规则表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysDataPermissionRule")
@RequiredArgsConstructor
@Schema(description = "数据权限规则控制器")
public class SysDataPermissionRuleController extends BaseController {

    private final ISysDataPermissionRuleService sysDataPermissionRuleService;
    private final ISysAppService sysAppService;

    /**
     * 添加 数据权限规则表
     *
     * @param sysDataPermissionRule 数据权限规则表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增数据权限规则")
    public ApiResult<Boolean> save(@RequestBody SysDataPermissionRuleEntity sysDataPermissionRule) {
        return ApiResult.success(sysDataPermissionRuleService.createRule(sysDataPermissionRule));
    }


    /**
     * 根据主键删除数据权限规则表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除数据权限规则")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysDataPermissionRuleService.removeById(id));
    }


    /**
     * 根据主键更新数据权限规则表
     *
     * @param sysDataPermissionRule 数据权限规则表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改数据权限规则")
    public ApiResult<Boolean> update(@RequestBody SysDataPermissionRuleEntity sysDataPermissionRule) {
        return ApiResult.success(sysDataPermissionRuleService.updateRule(sysDataPermissionRule));
    }


    /**
     * 查询所有数据权限规则表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取数据权限规则列表")
    public ApiResult<List<SysDataPermissionRuleEntity>> list() {
        return ApiResult.success(sysDataPermissionRuleService.list());
    }


    /**
     * 应用选项（id/名称/编码）
     */
    public record AppOption(String id, String name, String code) {
    }

    /**
     * 获取启用的应用列表（规则配置-应用ID下拉，来自 sys_app）
     *
     * @return 应用选项列表
     */
    @GetMapping("/apps")
    @Schema(description = "获取应用列表")
    public ApiResult<List<AppOption>> apps() {
        List<SysAppEntity> apps = sysAppService.list(QueryWrapper.create()
                .where(SysAppEntity::getStatus).eq(0)
                .and(SysAppEntity::getDeleted).eq(0)
                .orderBy(SysAppEntity::getAppName).asc());
        List<AppOption> options = apps.stream()
                .map(app -> new AppOption(
                        app.getId() != null ? app.getId().toString() : null,
                        app.getAppName(),
                        app.getAppCode()))
                .toList();
        return ApiResult.success(options);
    }

    /**
     * 查询当前数据库的所有基础表名（规则配置-目标表下拉）
     *
     * @return 表名列表
     */
    @GetMapping("/tables")
    @Schema(description = "获取数据库表名列表")
    public ApiResult<List<String>> tables() {
        return ApiResult.success(sysDataPermissionRuleService.listDatabaseTables());
    }

    /**
     * 查询指定表的列名（规则配置-字段下拉，随目标表级联加载）
     *
     * @param table 表名
     * @return 列名列表
     */
    @GetMapping("/columns")
    @Schema(description = "获取指定表的列名列表")
    public ApiResult<List<String>> columns(@RequestParam("table") String table) {
        return ApiResult.success(sysDataPermissionRuleService.listColumnsByTable(table));
    }

    /**
     * 根据数据权限规则表主键获取详细信息。
     *
     * @param id sysDataPermissionRule主键
     * @return 数据权限规则表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取数据权限规则详情")
    public ApiResult<SysDataPermissionRuleEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysDataPermissionRuleService.getById(id));
    }


    /**
     * 分页查询数据权限规则表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询数据权限规则")
    public ApiResult<Page<SysDataPermissionRuleEntity>> page(Page<SysDataPermissionRuleEntity> page) {
        return ApiResult.success(sysDataPermissionRuleService.page(page));
    }
}
