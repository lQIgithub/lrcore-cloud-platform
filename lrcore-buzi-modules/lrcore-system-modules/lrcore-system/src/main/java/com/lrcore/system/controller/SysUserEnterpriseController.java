package com.lrcore.system.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.mybatisflex.core.paginate.Page;
import com.lrcore.system.domain.SysUserEnterpriseEntity;
import com.lrcore.system.service.ISysUserEnterpriseService;
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
 * 用户-企业关联表 控制层。
 *
 * @author mybatis-flex-helper automatic generation
 * @since 1.0
 */
@RestController
@RequestMapping("/sysUserEnterprise")
@RequiredArgsConstructor
@Schema(description = "用户企业关联控制器")
public class SysUserEnterpriseController extends BaseController {

    private final ISysUserEnterpriseService sysUserEnterpriseService;

    /**
     * 添加 用户-企业关联表
     *
     * @param sysUserEnterprise 用户-企业关联表
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    @PostMapping("/save")
    @Schema(description = "新增用户企业关联")
    public ApiResult<Boolean> save(@RequestBody SysUserEnterpriseEntity sysUserEnterprise) {
        return ApiResult.success(sysUserEnterpriseService.save(sysUserEnterprise));
    }


    /**
     * 根据主键删除用户-企业关联表
     *
     * @param id 主键
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    @DeleteMapping("/remove/{id}")
    @Schema(description = "删除用户企业关联")
    public ApiResult<Boolean> remove(@PathVariable Serializable id) {
        return ApiResult.success(sysUserEnterpriseService.removeById(id));
    }


    /**
     * 根据主键更新用户-企业关联表
     *
     * @param sysUserEnterprise 用户-企业关联表
     * @return {@code true} 更新成功，{@code false} 更新失败
     */
    @PutMapping("/update")
    @Schema(description = "修改用户企业关联")
    public ApiResult<Boolean> update(@RequestBody SysUserEnterpriseEntity sysUserEnterprise) {
        return ApiResult.success(sysUserEnterpriseService.updateById(sysUserEnterprise));
    }


    /**
     * 查询所有用户-企业关联表
     *
     * @return 所有数据
     */
    @GetMapping("/list")
    @Schema(description = "获取用户企业关联列表")
    public ApiResult<List<SysUserEnterpriseEntity>> list() {
        return ApiResult.success(sysUserEnterpriseService.list());
    }


    /**
     * 根据用户-企业关联表主键获取详细信息。
     *
     * @param id sysUserEnterprise主键
     * @return 用户-企业关联表详情
     */
    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取用户企业关联详情")
    public ApiResult<SysUserEnterpriseEntity> getInfo(@PathVariable Serializable id) {
        return ApiResult.success(sysUserEnterpriseService.getById(id));
    }


    /**
     * 分页查询用户-企业关联表
     *
     * @param page 分页对象
     * @return 分页对象
     */
    @GetMapping("/page")
    @Schema(description = "分页查询用户企业关联")
    public ApiResult<Page<SysUserEnterpriseEntity>> page(Page<SysUserEnterpriseEntity> page) {
        return ApiResult.success(sysUserEnterpriseService.page(page));
    }
}
