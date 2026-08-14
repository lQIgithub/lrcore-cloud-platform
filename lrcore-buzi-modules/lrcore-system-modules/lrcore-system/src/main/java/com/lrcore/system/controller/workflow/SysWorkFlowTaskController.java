package com.lrcore.system.controller.workflow;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.flowable.model.task.*;
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
@RequestMapping("/api/v1/workflow/processTask")
@RequiredArgsConstructor
@Schema(description = "流程任务控制器")
public class SysWorkFlowTaskController extends BaseController {


    @GetMapping("/list")
    @Schema(description = "查询待办任务")
    public ApiResult<List<ProcessTaskVO>> list(QueryTaskVi queryTaskVi) {
        return ApiResult.success();
    }

    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取任务详情")
    public ApiResult<ProcessTaskVO> getInfo(@PathVariable("id") Serializable id) {
        return ApiResult.success();
    }


    @PostMapping("/claim")
    @Schema(description = "签收任务")
    public ApiResult<?> claim(@RequestBody ClaimTaskVi claimTaskVi) {

        return ApiResult.success();
    }

    @PostMapping("/complete")
    @Schema(description = "完成任务")
    public ApiResult<Boolean> complete(@RequestBody CompleteTaskVi completeTaskVi) {

        return ApiResult.success();
    }

    @PostMapping("/transfer")
    @Schema(description = "转办任务")
    public ApiResult<Boolean> transfer(@RequestBody TransferaskVi transferaskVi) {

        return ApiResult.success();
    }


}
