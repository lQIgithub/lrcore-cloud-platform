package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysColumnPermissionRuleEntity;
import com.lrcore.system.service.ISysColumnPermissionRuleService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

/**
 * 字段权限规则表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysColumnPermissionRule")
@RequiredArgsConstructor
@Schema(description = "字段权限规则控制器")
public class SysColumnPermissionRuleController extends BaseController {

    private final ISysColumnPermissionRuleService sysColumnPermissionRuleService;

    /**
     * 添加 字段权限规则表
     *
     * @param sysColumnPermissionRule 字段权限规则表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增字段权限规则")
    public ApiResult<Boolean> save(@RequestBody SysColumnPermissionRuleEntity sysColumnPermissionRule) {
        return ApiResult.success(sysColumnPermissionRuleService.save(sysColumnPermissionRule));
    }


    /**
     * 根据主键删除字段权限规则表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除字段权限规则")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysColumnPermissionRuleService.removeById(id));
    }


    /**
     * 根据主键更新字段权限规则表
     *
     * @param sysColumnPermissionRule 字段权限规则表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改字段权限规则")
    public ApiResult<Boolean> update(@RequestBody SysColumnPermissionRuleEntity sysColumnPermissionRule) {
        return ApiResult.success(sysColumnPermissionRuleService.updateById(sysColumnPermissionRule));
    }


    /**
     * 查询所有字段权限规则表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取字段权限规则列表")
    public ApiResult<List<SysColumnPermissionRuleEntity>> list() {
        return ApiResult.success(sysColumnPermissionRuleService.list());
    }


    /**
     * 根据字段权限规则表主键获取详细信息。
     *
     * @param id sysColumnPermissionRule主键
     * @return 字段权限规则表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取字段权限规则详情")
    public ApiResult<SysColumnPermissionRuleEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysColumnPermissionRuleService.getById(id));
    }


    /**
     * 分页查询字段权限规则表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询字段权限规则")
    public ApiResult<Page<SysColumnPermissionRuleEntity>> page(Page<SysColumnPermissionRuleEntity> page) {
        return ApiResult.success(sysColumnPermissionRuleService.page(page));
    }
}
