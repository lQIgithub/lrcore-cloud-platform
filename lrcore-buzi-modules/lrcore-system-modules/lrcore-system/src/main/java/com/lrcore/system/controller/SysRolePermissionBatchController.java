package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysRolePermissionBatchEntity;
import com.lrcore.system.service.ISysRolePermissionBatchService;
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
 * 角色权限批量操作记录表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysRolePermissionBatch")
@RequiredArgsConstructor
@Schema(description = "角色权限批量操作记录控制器")
public class SysRolePermissionBatchController extends BaseController {

    private final ISysRolePermissionBatchService sysRolePermissionBatchService;

    /**
     * 添加 角色权限批量操作记录表
     *
     * @param sysRolePermissionBatch 角色权限批量操作记录表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增角色权限批量操作记录")
    public ApiResult<Boolean> save(@RequestBody SysRolePermissionBatchEntity sysRolePermissionBatch) {
        return ApiResult.success(sysRolePermissionBatchService.save(sysRolePermissionBatch));
    }


    /**
     * 根据主键删除角色权限批量操作记录表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除角色权限批量操作记录")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysRolePermissionBatchService.removeById(id));
    }


    /**
     * 根据主键更新角色权限批量操作记录表
     *
     * @param sysRolePermissionBatch 角色权限批量操作记录表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改角色权限批量操作记录")
    public ApiResult<Boolean> update(@RequestBody SysRolePermissionBatchEntity sysRolePermissionBatch) {
        return ApiResult.success(sysRolePermissionBatchService.updateById(sysRolePermissionBatch));
    }


    /**
     * 查询所有角色权限批量操作记录表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取角色权限批量操作记录列表")
    public ApiResult<List<SysRolePermissionBatchEntity>> list() {
        return ApiResult.success(sysRolePermissionBatchService.list());
    }


    /**
     * 根据角色权限批量操作记录表主键获取详细信息。
     *
     * @param id sysRolePermissionBatch主键
     * @return 角色权限批量操作记录表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取角色权限批量操作记录详情")
    public ApiResult<SysRolePermissionBatchEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysRolePermissionBatchService.getById(id));
    }


    /**
     * 分页查询角色权限批量操作记录表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询角色权限批量操作记录")
    public ApiResult<Page<SysRolePermissionBatchEntity>> page(Page<SysRolePermissionBatchEntity> page) {
        return ApiResult.success(sysRolePermissionBatchService.page(page));
    }
}
