package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysTenantAppEntity;
import com.lrcore.system.service.ISysTenantAppService;
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
 * 租户-应用app关联表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysTenantApp")
@RequiredArgsConstructor
@Schema(description = "租户应用关联控制器")
public class SysTenantAppController extends BaseController {

    private final ISysTenantAppService sysTenantAppService;

    /**
     * 添加 租户-应用app关联表
     *
     * @param sysTenantApp 租户-应用app关联表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增租户应用关联")
    public ApiResult<Boolean> save(@RequestBody SysTenantAppEntity sysTenantApp) {
        return ApiResult.success(sysTenantAppService.save(sysTenantApp));
    }


    /**
     * 根据主键删除租户-应用app关联表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除租户应用关联")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysTenantAppService.removeById(id));
    }


    /**
     * 根据主键更新租户-应用app关联表
     *
     * @param sysTenantApp 租户-应用app关联表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改租户应用关联")
    public ApiResult<Boolean> update(@RequestBody SysTenantAppEntity sysTenantApp) {
        return ApiResult.success(sysTenantAppService.updateById(sysTenantApp));
    }


    /**
     * 查询所有租户-应用app关联表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取租户应用关联列表")
    public ApiResult<List<SysTenantAppEntity>> list() {
        return ApiResult.success(sysTenantAppService.list());
    }


    /**
     * 根据租户-应用app关联表主键获取详细信息。
     *
     * @param id sysTenantApp主键
     * @return 租户-应用app关联表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取租户应用关联详情")
    public ApiResult<SysTenantAppEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysTenantAppService.getById(id));
    }


    /**
     * 分页查询租户-应用app关联表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询租户应用关联")
    public ApiResult<Page<SysTenantAppEntity>> page(Page<SysTenantAppEntity> page) {
        return ApiResult.success(sysTenantAppService.page(page));
    }
}
