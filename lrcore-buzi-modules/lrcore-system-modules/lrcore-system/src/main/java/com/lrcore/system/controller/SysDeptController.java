package com.lrcore.system.controller;

import com.lrcore.common.annotations.annotation.Idempotent;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.core.web.domain.TreeNode;
import com.lrcore.system.domain.SysDeptEntity;
import com.lrcore.system.service.ISysDeptService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * 部门 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/depts")
@RequiredArgsConstructor
@Schema(description = "部门管理控制器")
public class SysDeptController extends BaseController {

    private final ISysDeptService sysDeptService;

    @GetMapping("/options")
    @Schema(description = "获取登录用户管理企业及部门树形结构")
    public ApiResult<List<TreeNode>> getDeptTree() {
        return ApiResult.success(sysDeptService.getDeptTree());
    }


    /**
     * 添加 部门
     * 举例: "dto.orderNo", "'pay:' +#orderId"
     *
     * @param sysDept 部门
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增部门")
    @Idempotent(prefix = "dept:save:", key = "#sysDept.deptCode")
    public ApiResult<Boolean> save(@RequestBody SysDeptEntity sysDept) {
        return ApiResult.success(sysDeptService.save(sysDept));
    }


    /**
     * 根据主键删除部门
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除部门")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysDeptService.removeById(id));
    }


    /**
     * 根据主键更新部门
     *
     * @param sysDept 部门
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改部门")
    public ApiResult<Boolean> update(@RequestBody SysDeptEntity sysDept) {
        return ApiResult.success(sysDeptService.updateById(sysDept));
    }


    /**
     * 查询所有部门
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取部门列表")
    public ApiResult<List<SysDeptEntity>> list() {
        return ApiResult.success(sysDeptService.list());
    }


    /**
     * 根据部门主键获取详细信息。
     *
     * @param id sysDept主键
     * @return 部门详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取部门详情")
    public ApiResult<SysDeptEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysDeptService.getById(id));
    }


    /**
     * 分页查询部门
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询部门")
    public ApiResult<Page<SysDeptEntity>> page(Page<SysDeptEntity> page) {
        return ApiResult.success(sysDeptService.page(page));
    }
}
