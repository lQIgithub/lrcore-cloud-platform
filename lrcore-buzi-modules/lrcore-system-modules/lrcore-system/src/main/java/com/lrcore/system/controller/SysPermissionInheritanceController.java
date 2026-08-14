package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysPermissionInheritanceEntity;
import com.lrcore.system.service.ISysPermissionInheritanceService;
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
 * 权限继承关系表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysPermissionInheritance")
@RequiredArgsConstructor
@Schema(description = "权限继承关系控制器")
public class SysPermissionInheritanceController extends BaseController {

    private final ISysPermissionInheritanceService sysPermissionInheritanceService;

    /**
     * 添加 权限继承关系表
     *
     * @param sysPermissionInheritance 权限继承关系表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增权限继承关系")
    public ApiResult<Boolean> save(@RequestBody SysPermissionInheritanceEntity sysPermissionInheritance) {
        return ApiResult.success(sysPermissionInheritanceService.save(sysPermissionInheritance));
    }


    /**
     * 根据主键删除权限继承关系表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除权限继承关系")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysPermissionInheritanceService.removeById(id));
    }


    /**
     * 根据主键更新权限继承关系表
     *
     * @param sysPermissionInheritance 权限继承关系表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改权限继承关系")
    public ApiResult<Boolean> update(@RequestBody SysPermissionInheritanceEntity sysPermissionInheritance) {
        return ApiResult.success(sysPermissionInheritanceService.updateById(sysPermissionInheritance));
    }


    /**
     * 查询所有权限继承关系表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取权限继承关系列表")
    public ApiResult<List<SysPermissionInheritanceEntity>> list() {
        return ApiResult.success(sysPermissionInheritanceService.list());
    }


    /**
     * 根据权限继承关系表主键获取详细信息。
     *
     * @param id sysPermissionInheritance主键
     * @return 权限继承关系表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取权限继承关系详情")
    public ApiResult<SysPermissionInheritanceEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysPermissionInheritanceService.getById(id));
    }


    /**
     * 分页查询权限继承关系表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询权限继承关系")
    public ApiResult<Page<SysPermissionInheritanceEntity>> page(Page<SysPermissionInheritanceEntity> page) {
        return ApiResult.success(sysPermissionInheritanceService.page(page));
    }
}
