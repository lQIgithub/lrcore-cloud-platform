package com.lrcore.system.controller.workflow;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.flowable.model.instance.ProcessInstanceVO;
import com.lrcore.common.flowable.model.instance.StartInstanceVi;
import com.lrcore.system.service.workflow.IWorkflowInstanceService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程实例 控制器（RESTful：列表/启动/详情/变量/终止/挂起激活）
 * @ClassName: SysWorkFlowInstanceController
 * @Author: Qi Liu
 * @Date: 2026/8/6 23:28
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/v1/workflow/processInstance")
@RequiredArgsConstructor
@Schema(description = "流程实例控制器")
public class SysWorkFlowInstanceController extends BaseController {

    private final IWorkflowInstanceService workflowInstanceService;

    @GetMapping
    @Schema(description = "我的申请列表（当前用户发起），分页返回 {list, total}")
    public ApiResult<Map<String, Object>> list(
            @RequestParam(required = false) String processDefinitionKey,
            @RequestParam(required = false) String businessKey,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize) {
        return ApiResult.success(workflowInstanceService.list(processDefinitionKey, businessKey, status, pageNum, pageSize));
    }

    @PostMapping("/start")
    @Schema(description = "启动流程实例（请假申请）")
    public ApiResult<ProcessInstanceVO> start(@RequestBody StartInstanceVi startInstanceVi) {
        return ApiResult.success(workflowInstanceService.start(startInstanceVi));
    }

    @GetMapping("/{id}")
    @Schema(description = "获取流程实例详情")
    public ApiResult<ProcessInstanceVO> getInfo(@PathVariable("id") String id) {
        return ApiResult.success(workflowInstanceService.getInfo(id));
    }

    @DeleteMapping("/{id}")
    @Schema(description = "终止并删除流程实例")
    public ApiResult<Boolean> delete(@PathVariable("id") String id) {
        workflowInstanceService.delete(id);
        return ApiResult.success(true);
    }

    @GetMapping("/{id}/variables")
    @Schema(description = "查询流程变量")
    public ApiResult<Map<String, Object>> getVariables(@PathVariable("id") String id) {
        return ApiResult.success(workflowInstanceService.getVariables(id));
    }

    @PutMapping("/{id}/variables")
    @Schema(description = "设置流程变量")
    public ApiResult<Boolean> setVariables(@PathVariable("id") String id,
                                           @RequestBody Map<String, Object> variables) {
        workflowInstanceService.setVariables(id, variables);
        return ApiResult.success(true);
    }

    @PostMapping("/{id}/suspend")
    @Schema(description = "挂起流程实例")
    public ApiResult<Boolean> suspend(@PathVariable("id") String id) {
        workflowInstanceService.suspend(id);
        return ApiResult.success(true);
    }

    @PostMapping("/{id}/activate")
    @Schema(description = "激活流程实例")
    public ApiResult<Boolean> activate(@PathVariable("id") String id) {
        workflowInstanceService.activate(id);
        return ApiResult.success(true);
    }

}
