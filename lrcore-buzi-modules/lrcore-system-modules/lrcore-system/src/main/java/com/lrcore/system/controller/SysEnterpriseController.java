package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.system.domain.SysEnterpriseEntity;
import com.lrcore.system.service.ISysEnterpriseService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * 企业信息表 控制层
 *
 * @author lrcore
 */
@RestController
@RequestMapping("/system/enterprise")
@RequiredArgsConstructor
@Schema(description = "企业管理控制器")
public class SysEnterpriseController extends BaseController {

    private final ISysEnterpriseService sysEnterpriseService;

    /**
     * 添加企业信息
     *
     * @param sysEnterprise 企业信息
     * @return 操作结果
     */
    @PostMapping("/save")
    @Schema(description = "添加企业")
    public ApiResult<Void> save(@Validated @RequestBody SysEnterpriseEntity sysEnterprise) {
        sysEnterpriseService.save(sysEnterprise);
        return ApiResult.success();
    }

    /**
     * 根据主键删除企业信息
     *
     * @param id 主键ID
     * @return 操作结果
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除企业")
    public ApiResult<Void> remove(@PathVariable Serializable id) {
        sysEnterpriseService.removeById(id);
        return ApiResult.success();
    }

    /**
     * 根据主键更新企业信息
     *
     * @param sysEnterprise 企业信息
     * @return 操作结果
     */
    @PutMapping("/update")
    @Schema(description = "更新企业")
    public ApiResult<Void> update(@Validated @RequestBody SysEnterpriseEntity sysEnterprise) {
        sysEnterpriseService.updateById(sysEnterprise);
        return ApiResult.success();
    }

    /**
     * 查询所有企业信息
     *
     * @return 所有企业列表
     */
    @GetMapping("/list")
    @Schema(description = "查询所有企业")
    public ApiResult<List<SysEnterpriseEntity>> list() {
        List<SysEnterpriseEntity> list = sysEnterpriseService.list();
        return ApiResult.success(list);
    }

    /**
     * 根据主键获取企业详细信息
     *
     * @param id 主键ID
     * @return 企业详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取企业详情")
    public ApiResult<SysEnterpriseEntity> getInfo(@PathVariable Serializable id) {
        SysEnterpriseEntity enterprise = sysEnterpriseService.getById(id);
        return ApiResult.success(enterprise);
    }

    /**
     * 分页查询企业信息
     *
     * @param page 分页对象
     * @return 分页结果
     */
    @GetMapping("/page")
    @Schema(description = "分页查询企业")
    public ApiResult<Page<SysEnterpriseEntity>> page(Page<SysEnterpriseEntity> page) {
        Page<SysEnterpriseEntity> result = sysEnterpriseService.page(page);
        return ApiResult.success(result);
    }

    /**
     * 查询企业树形结构
     *
     * @param enterpriseId 企业ID（可选）
     * @return 企业树列表
     */
    @GetMapping("/tree")
    @Schema(description = "查询企业树形结构")
    public ApiResult<List<SysEnterpriseEntity>> tree(@RequestParam(required = false) Long enterpriseId) {
        List<SysEnterpriseEntity> tree = sysEnterpriseService.selectEnterpriseTree(enterpriseId);
        return ApiResult.success(tree);
    }
}
