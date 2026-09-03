package com.lrcore.system.controller.workflow;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.flowable.model.task.ClaimTaskVi;
import com.lrcore.common.flowable.model.task.CompleteTaskVi;
import com.lrcore.common.flowable.model.task.ProcessTaskVO;
import com.lrcore.common.flowable.model.task.TransferaskVi;
import com.lrcore.system.service.workflow.IWorkflowTaskService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程任务 控制器（RESTful：待办/详情/签收/通过/驳回/转办）
 * @ClassName: SysWorkFlowTaskController
 * @Author: Qi Liu
 * @Date: 2026/8/6 23:28
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/v1/workflow/processTask")
@RequiredArgsConstructor
@Schema(description = "流程任务控制器")
public class SysWorkFlowTaskController extends BaseController {

    private final IWorkflowTaskService workflowTaskService;

    @GetMapping
    @Schema(description = "待办任务列表（当前用户），分页返回 {list, total}")
    public ApiResult<Map<String, Object>> list(
            @RequestParam(required = false) String processInstanceId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize) {
        return ApiResult.success(workflowTaskService.list(processInstanceId, status, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @Schema(description = "获取任务详情")
    public ApiResult<ProcessTaskVO> getInfo(@PathVariable("id") String id) {
        return ApiResult.success(workflowTaskService.getInfo(id));
    }

    @PostMapping("/{id}/claim")
    @Schema(description = "签收任务")
    public ApiResult<Boolean> claim(@PathVariable("id") String id, @RequestBody(required = false) ClaimTaskVi claimTaskVi) {
        workflowTaskService.claim(id);
        return ApiResult.success(true);
    }

    @PostMapping("/{id}/complete")
    @Schema(description = "通过：完成任务（variables 传 approved/comment）")
    public ApiResult<Boolean> complete(@PathVariable("id") String id, @RequestBody CompleteTaskVi completeTaskVi) {
        workflowTaskService.complete(id, completeTaskVi != null ? completeTaskVi.getVariables() : null);
        return ApiResult.success(true);
    }

    @PostMapping("/{id}/reject")
    @Schema(description = "驳回：任务回退到上游节点（body: {comment}）")
    public ApiResult<Boolean> reject(@PathVariable("id") String id, @RequestBody(required = false) Map<String, String> body) {
        workflowTaskService.reject(id, body != null ? body.get("comment") : null);
        return ApiResult.success(true);
    }

    @PostMapping("/{id}/transfer")
    @Schema(description = "转办任务（暂未实现）")
    public ApiResult<Boolean> transfer(@PathVariable("id") String id, @RequestBody(required = false) TransferaskVi transferaskVi) {
        workflowTaskService.transfer(id, transferaskVi != null ? transferaskVi.getTargetUserId() : null);
        return ApiResult.success(true);
    }

}
