package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysAppEntity;
import com.lrcore.system.service.ISysAppService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * 应用系统表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysApp")
@RequiredArgsConstructor
@Schema(description = "应用系统控制器")
public class SysAppController extends BaseController {

    private final ISysAppService sysAppService;

    /**
     * 添加 应用系统表
     *
     * @param sysApp 应用系统表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增应用系统")
    public ApiResult<Boolean> save(@RequestBody SysAppEntity sysApp) {
        return ApiResult.success(sysAppService.save(sysApp));
    }


    /**
     * 根据主键删除应用系统表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除应用系统")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysAppService.removeById(id));
    }


    /**
     * 根据主键更新应用系统表
     *
     * @param sysApp 应用系统表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改应用系统")
    public ApiResult<Boolean> update(@RequestBody SysAppEntity sysApp) {
        return ApiResult.success(sysAppService.updateById(sysApp));
    }


    /**
     * 查询所有应用系统表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取应用系统列表")
    public ApiResult<List<SysAppEntity>> list() {
        return ApiResult.success(sysAppService.list());
    }


    /**
     * 根据应用系统表主键获取详细信息。
     *
     * @param id sysApp主键
     * @return 应用系统表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取应用系统详情")
    public ApiResult<SysAppEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysAppService.getById(id));
    }


    /**
     * 分页查询应用系统表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询应用系统")
    public ApiResult<Page<SysAppEntity>> page(Page<SysAppEntity> page) {
        return ApiResult.success(sysAppService.page(page));
    }
}
