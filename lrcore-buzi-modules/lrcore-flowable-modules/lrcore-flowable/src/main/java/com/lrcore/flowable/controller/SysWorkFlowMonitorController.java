package com.lrcore.flowable.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.flowable.model.task.ProcessTaskVO;
import com.lrcore.common.flowable.model.task.QueryTaskVi;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程监控 控制器
 * @ClassName: SysWorkFlowMonitorController
 * @Author: Qi Liu
 * @Date: 2026/8/6 23:28
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/v1/workflow/processMonitor")
@RequiredArgsConstructor
@Schema(description = "流程监控控制器")
public class SysWorkFlowMonitorController extends BaseController {


    @GetMapping("/getStatistics")
    @Schema(description = "获取流程统计数据")
    public ApiResult<List<ProcessTaskVO>> getStatistics(QueryTaskVi queryTaskVi) {
        return ApiResult.success();
    }

    @GetMapping("/getActiveInstances")
    @Schema(description = "获取活跃实例统计")
    public ApiResult<ProcessTaskVO> getActiveInstances(@RequestParam("id") Serializable id) {
        return ApiResult.success();
    }

    @GetMapping("/getTaskStatistics")
    @Schema(description = "获取任务统计")
    public ApiResult<ProcessTaskVO> getTaskStatistics(@RequestParam("id") Serializable id) {
        return ApiResult.success();
    }


}
