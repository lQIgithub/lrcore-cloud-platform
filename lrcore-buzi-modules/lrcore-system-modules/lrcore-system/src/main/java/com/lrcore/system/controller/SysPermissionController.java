package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.login.SysMenuInfo;
import com.lrcore.system.domain.SysPermissionEntity;
import com.lrcore.system.service.ISysPermissionService;
import com.lrcore.system.service.ISysUserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 权限路由信息控制器
 * @ClassName: SysPermissionController
 * @Author: Qi Liu
 * @Date: 2026/7/26 09:11
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/v1/menus")
@RequiredArgsConstructor
@Schema(description = "权限管理控制器")
public class SysPermissionController extends BaseController {

    private final ISysPermissionService sysPermissionService;
    private final ISysUserService sysUserService;

    /**
     * 获取当前用户的路由列表
     *
     * @return 所有数据
     */
    @GetMapping("/routes")
    @Schema(description = "获取当前用户的路由列表")
    public ApiResult<List<SysMenuInfo>> routes() {
        List<SysMenuInfo> menus = sysUserService.getSysMenuInfoList();
        return ApiResult.success(menus);
    }

    // *****************************************************************************************

    /**
     * 添加 权限信息表
     *
     * @param sysPermission 权限信息表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增权限")
    public ApiResult<Boolean> save(@RequestBody SysPermissionEntity sysPermission) {
        return ApiResult.success(sysPermissionService.save(sysPermission));
    }


    /**
     * 根据主键删除权限信息表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除权限")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysPermissionService.removeById(id));
    }


    /**
     * 根据主键更新权限信息表
     *
     * @param sysPermission 权限信息表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改权限")
    public ApiResult<Boolean> update(@RequestBody SysPermissionEntity sysPermission) {
        return ApiResult.success(sysPermissionService.updateById(sysPermission));
    }


    /**
     * 根据权限信息表主键获取详细信息。
     *
     * @param id sysPermission主键
     * @return 权限信息表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取权限详情")
    public ApiResult<SysPermissionEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysPermissionService.getById(id));
    }


    /**
     * 分页查询权限信息表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询权限")
    public ApiResult<Page<SysPermissionEntity>> page(Page<SysPermissionEntity> page) {
        return ApiResult.success(sysPermissionService.page(page));
    }
}
