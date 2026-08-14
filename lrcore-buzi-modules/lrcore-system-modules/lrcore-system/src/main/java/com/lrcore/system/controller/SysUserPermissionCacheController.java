package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysUserPermissionCacheEntity;
import com.lrcore.system.service.ISysUserPermissionCacheService;
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
 * 用户权限缓存表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysUserPermissionCache")
@RequiredArgsConstructor
@Schema(description = "用户权限缓存控制器")
public class SysUserPermissionCacheController extends BaseController {

    private final ISysUserPermissionCacheService sysUserPermissionCacheService;

    /**
     * 添加 用户权限缓存表
     *
     * @param sysUserPermissionCache 用户权限缓存表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增用户权限缓存")
    public ApiResult<Boolean> save(@RequestBody SysUserPermissionCacheEntity sysUserPermissionCache) {
        return ApiResult.success(sysUserPermissionCacheService.save(sysUserPermissionCache));
    }


    /**
     * 根据主键删除用户权限缓存表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除用户权限缓存")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysUserPermissionCacheService.removeById(id));
    }


    /**
     * 根据主键更新用户权限缓存表
     *
     * @param sysUserPermissionCache 用户权限缓存表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改用户权限缓存")
    public ApiResult<Boolean> update(@RequestBody SysUserPermissionCacheEntity sysUserPermissionCache) {
        return ApiResult.success(sysUserPermissionCacheService.updateById(sysUserPermissionCache));
    }


    /**
     * 查询所有用户权限缓存表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取用户权限缓存列表")
    public ApiResult<List<SysUserPermissionCacheEntity>> list() {
        return ApiResult.success(sysUserPermissionCacheService.list());
    }


    /**
     * 根据用户权限缓存表主键获取详细信息。
     *
     * @param id sysUserPermissionCache主键
     * @return 用户权限缓存表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取用户权限缓存详情")
    public ApiResult<SysUserPermissionCacheEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysUserPermissionCacheService.getById(id));
    }


    /**
     * 分页查询用户权限缓存表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询用户权限缓存")
    public ApiResult<Page<SysUserPermissionCacheEntity>> page(Page<SysUserPermissionCacheEntity> page) {
        return ApiResult.success(sysUserPermissionCacheService.page(page));
    }
}
