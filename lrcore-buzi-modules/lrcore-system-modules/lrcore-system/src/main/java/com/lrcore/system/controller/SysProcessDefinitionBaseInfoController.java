package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysProcessDefinitionBaseInfoEntity;
import com.lrcore.system.service.ISysProcessDefinitionBaseInfoService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程定义基础信息 控制器
 * @ClassName: SysProcessDefinitionBaseInfoController
 * @Author: lrcore
 * @Date: 2026/08/13
 * @Version: 1.0
 */
@RestController
@RequestMapping("/processDefinitionBaseInfo")
@RequiredArgsConstructor
@Schema(description = "流程定义基础信息控制器")
public class SysProcessDefinitionBaseInfoController extends BaseController {

    private final ISysProcessDefinitionBaseInfoService processDefinitionBaseInfoService;

    /**
     * 添加 流程定义基础信息
     *
     * @param entity 流程定义基础信息
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增流程定义基础信息")
    public ApiResult<Boolean> save(@RequestBody SysProcessDefinitionBaseInfoEntity entity) {
        return ApiResult.success(processDefinitionBaseInfoService.save(entity));
    }

    /**
     * 根据主键删除流程定义基础信息
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除流程定义基础信息")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(processDefinitionBaseInfoService.removeById(id));
    }

    /**
     * 根据主键更新流程定义基础信息
     *
     * @param entity 流程定义基础信息
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改流程定义基础信息")
    public ApiResult<Boolean> update(@RequestBody SysProcessDefinitionBaseInfoEntity entity) {
        return ApiResult.success(processDefinitionBaseInfoService.updateById(entity));
    }

    /**
     * 查询所有流程定义基础信息
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取流程定义基础信息列表")
    public ApiResult<List<SysProcessDefinitionBaseInfoEntity>> list() {
        return ApiResult.success(processDefinitionBaseInfoService.list());
    }

    /**
     * 根据主键获取流程定义基础信息详细信息
     *
     * @param id 主键
     * @return 流程定义基础信息详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取流程定义基础信息详情")
    public ApiResult<SysProcessDefinitionBaseInfoEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(processDefinitionBaseInfoService.getById(id));
    }

    /**
     * 分页查询流程定义基础信息
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询流程定义基础信息")
    public ApiResult<Page<SysProcessDefinitionBaseInfoEntity>> page(Page<SysProcessDefinitionBaseInfoEntity> page) {
        return ApiResult.success(processDefinitionBaseInfoService.page(page));
    }
}
