package com.lrcore.system.controller.workflow;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.flowable.model.form.FormDefinitionVO;
import com.lrcore.common.flowable.model.form.QueryFormVi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程表单 控制器
 * @ClassName: SysWorkFlowDefinitionController
 * @Author: Qi Liu
 * @Date: 2026/8/6 23:28
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/v1/workflow/processForm")
@RequiredArgsConstructor
@Schema(description = "流程表单控制器")
public class SysWorkFlowFormController extends BaseController {


    @GetMapping("/listByProcessByProcessKey")
    @Schema(description = "获取流程所有表单")
    public ApiResult<List<FormDefinitionVO>> page(@RequestParam String processKey) {
        return ApiResult.success();
    }

    @GetMapping("/getFormDefinitionByTask")
    @Schema(description = "获取表单定义")
    public ApiResult<FormDefinitionVO> getFormDefinitionByTask(QueryFormVi queryFormVi) {
        return ApiResult.success();
    }


    @PostMapping("/save")
    @Schema(description = "保存表单定义")
    public ApiResult<?> save(@RequestBody FormDefinitionVO formDefinitionVO) {

        return ApiResult.success();
    }


}
