package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysColumnMetadataEntity;
import com.lrcore.system.service.ISysColumnMetadataService;
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
 * 字段元数据表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysColumnMetadata")
@RequiredArgsConstructor
@Schema(description = "字段元数据控制器")
public class SysColumnMetadataController extends BaseController {

    private final ISysColumnMetadataService sysColumnMetadataService;

    /**
     * 添加 字段元数据表
     *
     * @param sysColumnMetadata 字段元数据表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增字段元数据")
    public ApiResult<Boolean> save(@RequestBody SysColumnMetadataEntity sysColumnMetadata) {
        return ApiResult.success(sysColumnMetadataService.save(sysColumnMetadata));
    }


    /**
     * 根据主键删除字段元数据表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除字段元数据")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysColumnMetadataService.removeById(id));
    }


    /**
     * 根据主键更新字段元数据表
     *
     * @param sysColumnMetadata 字段元数据表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改字段元数据")
    public ApiResult<Boolean> update(@RequestBody SysColumnMetadataEntity sysColumnMetadata) {
        return ApiResult.success(sysColumnMetadataService.updateById(sysColumnMetadata));
    }


    /**
     * 查询所有字段元数据表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取字段元数据列表")
    public ApiResult<List<SysColumnMetadataEntity>> list() {
        return ApiResult.success(sysColumnMetadataService.list());
    }


    /**
     * 根据字段元数据表主键获取详细信息。
     *
     * @param id sysColumnMetadata主键
     * @return 字段元数据表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取字段元数据详情")
    public ApiResult<SysColumnMetadataEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysColumnMetadataService.getById(id));
    }


    /**
     * 分页查询字段元数据表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询字段元数据")
    public ApiResult<Page<SysColumnMetadataEntity>> page(Page<SysColumnMetadataEntity> page) {
        return ApiResult.success(sysColumnMetadataService.page(page));
    }
}
