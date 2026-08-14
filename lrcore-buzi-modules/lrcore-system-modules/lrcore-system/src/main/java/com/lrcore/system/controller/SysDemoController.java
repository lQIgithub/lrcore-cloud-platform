package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysDemoEntity;
import com.lrcore.system.service.ISysDemoService;
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
 * 演示信息表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysDemo")
@RequiredArgsConstructor
@Schema(description = "演示管理控制器")
public class SysDemoController extends BaseController {

    private final ISysDemoService sysDemoService;

    /**
     * 添加 演示信息表
     *
     * @param sysDemo 演示信息表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增演示数据")
    public ApiResult<Boolean> save(@RequestBody SysDemoEntity sysDemo) {
        return ApiResult.success(sysDemoService.save(sysDemo));
    }


    /**
     * 根据主键删除演示信息表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除演示数据")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysDemoService.removeById(id));
    }


    /**
     * 根据主键更新演示信息表
     *
     * @param sysDemo 演示信息表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改演示数据")
    public ApiResult<Boolean> update(@RequestBody SysDemoEntity sysDemo) {
        return ApiResult.success(sysDemoService.updateById(sysDemo));
    }


    /**
     * 查询所有演示信息表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取演示数据列表")
    public ApiResult<List<SysDemoEntity>> list() {
        return ApiResult.success(sysDemoService.list());
    }


    /**
     * 根据演示信息表主键获取详细信息。
     *
     * @param id sysDemo主键
     * @return 演示信息表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取演示数据详情")
    public ApiResult<SysDemoEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysDemoService.getById(id));
    }


    /**
     * 分页查询演示信息表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询演示数据")
    public ApiResult<Page<SysDemoEntity>> page(Page<SysDemoEntity> page) {
        return ApiResult.success(sysDemoService.page(page));
    }
}
