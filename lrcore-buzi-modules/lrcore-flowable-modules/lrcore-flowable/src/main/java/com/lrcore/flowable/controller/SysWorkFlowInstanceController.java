package com.lrcore.flowable.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.flowable.model.definition.ProcessDefinitionVo;
import com.lrcore.common.flowable.model.instance.ProcessInstanceVO;
import com.lrcore.common.flowable.model.instance.QueryInstanceVi;
import com.lrcore.common.flowable.model.instance.SetVariablesInstanceVi;
import com.lrcore.common.flowable.model.instance.StartInstanceVi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程实例 控制器
 * @ClassName: SysWorkFlowDefinitionController
 * @Author: Qi Liu
 * @Date: 2026/8/6 23:28
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/v1/workflow/processInstance")
@RequiredArgsConstructor
@Schema(description = "流程实例控制器")
public class SysWorkFlowInstanceController extends BaseController {


    @GetMapping("/list")
    @Schema(description = "获取流程实例列表")
    public ApiResult<List<ProcessInstanceVO>> list(QueryInstanceVi queryInstanceVi) {
        return ApiResult.success();
    }

    @GetMapping("/getInfo")
    @Schema(description = "获取流程实例详情")
    public ApiResult<ProcessDefinitionVo> getInfo(@RequestParam("id") Serializable id) {
        return ApiResult.success();
    }

    @GetMapping("/getVariables")
    @Schema(description = "查询流程变量")
    public ApiResult<ProcessDefinitionVo> getVariables(@RequestParam("id") Serializable id) {
        return ApiResult.success();
    }

    @PostMapping("/start")
    @Schema(description = "启动流程实例")
    public ApiResult<ProcessInstanceVO> start(@RequestBody StartInstanceVi startInstanceVi) {

        return ApiResult.success();
    }

    @PostMapping("/setVariables")
    @Schema(description = "设置流程变量")
    public ApiResult<Boolean> setVariables(@RequestBody SetVariablesInstanceVi setVariablesInstanceVi) {

        return ApiResult.success();
    }

    @DeleteMapping("/delete/{id}")
    @Schema(description = "删除流程实例")
    public ApiResult<Boolean> delete(@PathVariable("id") Long id) {

        return ApiResult.success();
    }


    @PostMapping("/suspend")
    @Schema(description = "挂起流程实例")
    public ApiResult<Boolean> suspend(@RequestParam Long id) {

        return ApiResult.success();
    }

    @PostMapping("/activate")
    @Schema(description = "激活流程实例")
    public ApiResult<Boolean> activate(@RequestParam Long id) {

        return ApiResult.success();
    }

}
