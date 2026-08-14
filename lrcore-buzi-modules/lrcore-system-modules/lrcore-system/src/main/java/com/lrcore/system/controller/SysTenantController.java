package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysTenantEntity;
import com.lrcore.system.service.ISysTenantService;
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
 * 系统租户表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysTenant")
@RequiredArgsConstructor
@Schema(description = "系统租户控制器")
public class SysTenantController extends BaseController {

    private final ISysTenantService sysTenantService;

    /**
     * 添加 系统租户表
     *
     * @param sysTenant 系统租户表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增租户")
    public ApiResult<Boolean> save(@RequestBody SysTenantEntity sysTenant) {
        return ApiResult.success(sysTenantService.save(sysTenant));
    }


    /**
     * 根据主键删除系统租户表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除租户")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysTenantService.removeById(id));
    }


    /**
     * 根据主键更新系统租户表
     *
     * @param sysTenant 系统租户表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改租户")
    public ApiResult<Boolean> update(@RequestBody SysTenantEntity sysTenant) {
        return ApiResult.success(sysTenantService.updateById(sysTenant));
    }


    /**
     * 查询所有系统租户表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取租户列表")
    public ApiResult<List<SysTenantEntity>> list() {
        return ApiResult.success(sysTenantService.list());
    }


    /**
     * 根据系统租户表主键获取详细信息。
     *
     * @param id sysTenant主键
     * @return 系统租户表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取租户详情")
    public ApiResult<SysTenantEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysTenantService.getById(id));
    }


    /**
     * 分页查询系统租户表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询租户")
    public ApiResult<Page<SysTenantEntity>> page(Page<SysTenantEntity> page) {
        return ApiResult.success(sysTenantService.page(page));
    }
}
