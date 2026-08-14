package com.lrcore.rule.controller;

import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.rule.domain.model.RuleDto;
import com.lrcore.rule.service.IJarRuleLoadService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * <>类模块说明</p>
 *
 * @Describe: 规则控制器
 * @ClassName: RuleController
 * @Author: Qi Liu
 * @Date: 2026/8/4 11:36
 * @Version: 1.0
 */
@RestController
@RequestMapping("/api/v1/rule")
@RequiredArgsConstructor
@Schema(description = "规则控制器")
public class RuleController extends BaseController {

    private final IJarRuleLoadService jarRuleLoadService;

    @Qualifier("ruleManageExecutor")
    private final ExecutorService ruleManageExecutor;

    /**
     * <p>方法说明</p>
     *
     * @Describe: 重新加载规则 手动热更新接口：接收HTTP请求，立刻返回，把加载任务丢后台管理线程
     * HTTP快速响应，Jar加载耗时不会占用Tomcat线程、不会超时
     * @Param: []
     * @Author: Qi Liu
     * @Date: 2026/8/4 10:30
     * @Return com.lrcore.common.core.web.domain.ApiResult<java.lang.Boolean>
     * @Version: 1.0
     */
    @GetMapping("/reloadRule")
    @Schema(description = "重新加载规则")
    public ApiResult<String> reloadRule() {
        ruleManageExecutor.submit(jarRuleLoadService::scanAndLoadJarRule);
        return ApiResult.success("accept，后台执行reload");
    }

    /**
     * 定时扫描，也提交管理线程池执行，禁止运行在scheduler默认线程
     * 每8小时重新加载一次插件
     * 使用以下方式可以将cron表达式内容迁移到application.yml中
     * @Scheduled(cron = "${task.scan.cron:0 * * * * ?}")
     *   task:
     *     scan:
     *       cron: 0 * * * * ?
     */
    @Scheduled(cron = "0 * */8 * * ?")
    public void autoScan() {
        ruleManageExecutor.submit(jarRuleLoadService::scanAndLoadJarRule);
    }


    @GetMapping("/getRuleDtoList")
    @Schema(description = "获取规则列表")
    public ApiResult<List<RuleDto>> getRuleDtoList() {
        return jarRuleLoadService.getRuleDtoList();
    }
}
