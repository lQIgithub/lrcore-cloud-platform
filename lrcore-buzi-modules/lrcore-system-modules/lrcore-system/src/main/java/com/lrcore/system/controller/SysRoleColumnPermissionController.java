package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysRoleColumnPermissionEntity;
import com.lrcore.system.service.ISysRoleColumnPermissionService;
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
 * 角色字段权限关联表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysRoleColumnPermission")
@RequiredArgsConstructor
@Schema(description = "角色字段权限关联控制器")
public class SysRoleColumnPermissionController extends BaseController {

    private final ISysRoleColumnPermissionService sysRoleColumnPermissionService;

    /**
     * 添加 角色字段权限关联表
     *
     * @param sysRoleColumnPermission 角色字段权限关联表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增角色字段权限关联")
    public ApiResult<Boolean> save(@RequestBody SysRoleColumnPermissionEntity sysRoleColumnPermission) {
        return ApiResult.success(sysRoleColumnPermissionService.save(sysRoleColumnPermission));
    }


    /**
     * 根据主键删除角色字段权限关联表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除角色字段权限关联")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysRoleColumnPermissionService.removeById(id));
    }


    /**
     * 根据主键更新角色字段权限关联表
     *
     * @param sysRoleColumnPermission 角色字段权限关联表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改角色字段权限关联")
    public ApiResult<Boolean> update(@RequestBody SysRoleColumnPermissionEntity sysRoleColumnPermission) {
        return ApiResult.success(sysRoleColumnPermissionService.updateById(sysRoleColumnPermission));
    }


    /**
     * 查询所有角色字段权限关联表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取角色字段权限关联列表")
    public ApiResult<List<SysRoleColumnPermissionEntity>> list() {
        return ApiResult.success(sysRoleColumnPermissionService.list());
    }


    /**
     * 根据角色字段权限关联表主键获取详细信息。
     *
     * @param id sysRoleColumnPermission主键
     * @return 角色字段权限关联表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取角色字段权限关联详情")
    public ApiResult<SysRoleColumnPermissionEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysRoleColumnPermissionService.getById(id));
    }


    /**
     * 分页查询角色字段权限关联表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询角色字段权限关联")
    public ApiResult<Page<SysRoleColumnPermissionEntity>> page(Page<SysRoleColumnPermissionEntity> page) {
        return ApiResult.success(sysRoleColumnPermissionService.page(page));
    }
}
