package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysPermissionHistoryEntity;
import com.lrcore.system.service.ISysPermissionHistoryService;
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
 * 权限变更历史表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysPermissionHistory")
@RequiredArgsConstructor
@Schema(description = "权限变更历史控制器")
public class SysPermissionHistoryController extends BaseController {

    private final ISysPermissionHistoryService sysPermissionHistoryService;

    /**
     * 添加 权限变更历史表
     *
     * @param sysPermissionHistory 权限变更历史表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增权限变更历史")
    public ApiResult<Boolean> save(@RequestBody SysPermissionHistoryEntity sysPermissionHistory) {
        return ApiResult.success(sysPermissionHistoryService.save(sysPermissionHistory));
    }


    /**
     * 根据主键删除权限变更历史表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除权限变更历史")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysPermissionHistoryService.removeById(id));
    }


    /**
     * 根据主键更新权限变更历史表
     *
     * @param sysPermissionHistory 权限变更历史表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改权限变更历史")
    public ApiResult<Boolean> update(@RequestBody SysPermissionHistoryEntity sysPermissionHistory) {
        return ApiResult.success(sysPermissionHistoryService.updateById(sysPermissionHistory));
    }


    /**
     * 查询所有权限变更历史表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取权限变更历史列表")
    public ApiResult<List<SysPermissionHistoryEntity>> list() {
        return ApiResult.success(sysPermissionHistoryService.list());
    }


    /**
     * 根据权限变更历史表主键获取详细信息。
     *
     * @param id sysPermissionHistory主键
     * @return 权限变更历史表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取权限变更历史详情")
    public ApiResult<SysPermissionHistoryEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysPermissionHistoryService.getById(id));
    }


    /**
     * 分页查询权限变更历史表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询权限变更历史")
    public ApiResult<Page<SysPermissionHistoryEntity>> page(Page<SysPermissionHistoryEntity> page) {
        return ApiResult.success(sysPermissionHistoryService.page(page));
    }
}
