package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysPermissionAuditLogEntity;
import com.lrcore.system.service.ISysPermissionAuditLogService;
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
 * 权限审计日志表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysPermissionAuditLog")
@RequiredArgsConstructor
@Schema(description = "权限审计日志控制器")
public class SysPermissionAuditLogController extends BaseController {

    private final ISysPermissionAuditLogService sysPermissionAuditLogService;

    /**
     * 添加 权限审计日志表
     *
     * @param sysPermissionAuditLog 权限审计日志表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增权限审计日志")
    public ApiResult<Boolean> save(@RequestBody SysPermissionAuditLogEntity sysPermissionAuditLog) {
        return ApiResult.success(sysPermissionAuditLogService.save(sysPermissionAuditLog));
    }


    /**
     * 根据主键删除权限审计日志表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除权限审计日志")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysPermissionAuditLogService.removeById(id));
    }


    /**
     * 根据主键更新权限审计日志表
     *
     * @param sysPermissionAuditLog 权限审计日志表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改权限审计日志")
    public ApiResult<Boolean> update(@RequestBody SysPermissionAuditLogEntity sysPermissionAuditLog) {
        return ApiResult.success(sysPermissionAuditLogService.updateById(sysPermissionAuditLog));
    }


    /**
     * 查询所有权限审计日志表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取权限审计日志列表")
    public ApiResult<List<SysPermissionAuditLogEntity>> list() {
        return ApiResult.success(sysPermissionAuditLogService.list());
    }


    /**
     * 根据权限审计日志表主键获取详细信息。
     *
     * @param id sysPermissionAuditLog主键
     * @return 权限审计日志表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取权限审计日志详情")
    public ApiResult<SysPermissionAuditLogEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysPermissionAuditLogService.getById(id));
    }


    /**
     * 分页查询权限审计日志表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询权限审计日志")
    public ApiResult<Page<SysPermissionAuditLogEntity>> page(Page<SysPermissionAuditLogEntity> page) {
        return ApiResult.success(sysPermissionAuditLogService.page(page));
    }
}
