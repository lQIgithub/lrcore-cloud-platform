package com.lrcore.system.controller;

import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysUserEntity;
import com.lrcore.system.service.ISysUserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * 用户基础信息表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Schema(description = "用户管理控制器")
public class SysUserController extends BaseController {

    private final ISysUserService sysUserService;

    /**
     * 添加 用户基础信息表
     *
     * @param sysUser 用户基础信息表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增用户")
    public ApiResult<Boolean> save(@RequestBody SysUserEntity sysUser) {
        return ApiResult.success(sysUserService.save(sysUser));
    }


    /**
     * 根据主键删除用户基础信息表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除用户")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysUserService.removeById(id));
    }


    /**
     * 根据主键更新用户基础信息表
     *
     * @param sysUser 用户基础信息表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改用户")
    public ApiResult<Boolean> update(@RequestBody SysUserEntity sysUser) {
        return ApiResult.success(sysUserService.updateById(sysUser));
    }


    /**
     * 查询所有用户基础信息表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取用户列表")
    public ApiResult<List<SysUserEntity>> list() {
        return ApiResult.success(sysUserService.list());
    }


    /**
     * 根据用户基础信息表主键获取详细信息。
     *
     * @param id sysUser主键
     * @return 用户基础信息表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取用户详情")
    public ApiResult<SysUserEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysUserService.getById(id));
    }


    /**
     * 分页查询用户基础信息表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询用户")
    public ApiResult<Page<SysUserEntity>> page(Page<SysUserEntity> page) {
        return ApiResult.success(sysUserService.page(page));
    }
}
