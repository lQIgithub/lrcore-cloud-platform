package com.lrcore.flowable.controller;

import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.flowable.enums.ProcessDefinitionStatus;
import com.lrcore.common.flowable.model.definition.FlowGraphData;
import com.lrcore.common.flowable.model.definition.ProcessDefinitionVo;
import com.lrcore.common.flowable.model.definition.QueryDefinitionVi;
import com.lrcore.common.flowable.service.FlowConversionService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程定义 控制器
 * @ClassName: SysWorkFlowDefinitionController
 * @Author: Qi Liu
 * @Date: 2026/8/6 23:28
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/workflow/processDefinition")
@RequiredArgsConstructor
@Schema(description = "流程定义控制器")
public class SysWorkFlowDefinitionController extends BaseController {

    private final FlowConversionService flowConversionService;
    private final RepositoryService repositoryService;

    @GetMapping("/list")
    @Schema(description = "获取流程定义列表")
    public ApiResult<List<ProcessDefinitionVo>> page(QueryDefinitionVi queryDefinitionVi) {
        List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionVersion().desc()
                .list();

        List<ProcessDefinitionVo> list = processDefinitions.stream()
                .map(this::convertToVo)
                .collect(Collectors.toList());

        if (queryDefinitionVi != null) {
            String keyword = queryDefinitionVi.getKeyword();
            String status = queryDefinitionVi.getStatus();

            if (FunStrUtils.hasText(keyword)) {
                list = list.stream()
                        .filter(vo -> (vo.getName() != null && vo.getName().contains(keyword))
                                || (vo.getKey() != null && vo.getKey().contains(keyword))
                                || (vo.getDescription() != null && vo.getDescription().contains(keyword)))
                        .collect(Collectors.toList());
            }

            if (FunStrUtils.hasText(status)) {
                ProcessDefinitionStatus targetStatus = ProcessDefinitionStatus.valueOf(status);
                list = list.stream()
                        .filter(vo -> vo.getStatus() == targetStatus)
                        .collect(Collectors.toList());
            }
        }

        return ApiResult.success(list);
    }

    private ProcessDefinitionVo convertToVo(ProcessDefinition pd) {
        ProcessDefinitionVo vo = new ProcessDefinitionVo();
        vo.setId(pd.getId());
        vo.setKey(pd.getKey());
        vo.setName(pd.getName());
        vo.setDescription(pd.getDescription());
        vo.setCategory(pd.getCategory());
        vo.setVersion(pd.getVersion());
        vo.setStatus(ProcessDefinitionStatus.deployed);
        return vo;
    }

    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取流程定义详情")
    public ApiResult<ProcessDefinitionVo> getInfo(@PathVariable("id") Serializable id) {
        return ApiResult.success();
    }

    @PostMapping("/save")
    @Schema(description = "创建流程定义")
    public ApiResult<ProcessDefinitionVo> processDefinitionsSave(@RequestBody ProcessDefinitionVo processDefinitionVo) {
        log.info("创建流程定义");
        try {
            // 这里使用链式调用功能，做数据校验
            FlowGraphData graphData = processDefinitionVo.getGraphData();
            boolean isValid = flowConversionService.validateFlowGraphData(graphData);
            if (!isValid) {
                log.warn("流程图数据校验失败");
                return ApiResult.fail("流程图数据无效");
            }

            // 转换BPMN XML
            String bpmnXml = flowConversionService.convertToBpmnXml(graphData);
            if (FunStrUtils.isEmpty(bpmnXml)) {
                log.warn("BPMN XML为空");
                return ApiResult.fail("BPMN XML为空");
            }

            log.info("开始部署流程定义到Flowable引擎, key:{}", processDefinitionVo.getKey());
            Deployment deployment = repositoryService.createDeployment()
                    .key(processDefinitionVo.getKey())
                    .name(processDefinitionVo.getName())
                    .category(processDefinitionVo.getCategory())
                    .addInputStream(processDefinitionVo.getKey() + ".bpmn20.xml",
                            new java.io.ByteArrayInputStream(bpmnXml.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                    .deploy();

            log.info("流程定义部署成功, deploymentId:{}, key:{}", deployment.getId(), deployment.getKey());

            // 设置流程定义属性
            processDefinitionVo.setId(deployment.getId());
            processDefinitionVo.setBpmnXml(bpmnXml);
            processDefinitionVo.setCreateTime(java.time.LocalDateTime.now());
            processDefinitionVo.setUpdateTime(java.time.LocalDateTime.now());
            processDefinitionVo.setStatus(ProcessDefinitionStatus.draft);

            return ApiResult.success(processDefinitionVo);
        } catch (Exception e) {
            log.error("创建流程定义失败", e);
            return ApiResult.fail("创建流程定义失败: " + e.getMessage());
        }
    }

    @PostMapping("/update")
    @Schema(description = "更新流程定义")
    public ApiResult<Boolean> processDefinitionsUpdate(@RequestBody ProcessDefinitionVo processDefinitionVo) {
        return ApiResult.success();
    }

    @DeleteMapping("/delete/{id}")
    @Schema(description = "删除流程定义")
    public ApiResult<Boolean> processDefinitionsDelete(@PathVariable("id") Long id) {
        return ApiResult.success();
    }

    @PostMapping("/saveGraph")
    @Schema(description = "保存流程图数据")
    public ApiResult<Boolean> saveGraph(@RequestBody ProcessDefinitionVo processDefinitionVo) {
        return ApiResult.success();
    }

    @GetMapping("/exportBpmn")
    @Schema(description = "导出BPMN XML")
    public ApiResult<Boolean> exportBpmn(@RequestParam("id") Long id) {
        return ApiResult.success();
    }

    @PostMapping("/deploy")
    @Schema(description = "部署流程（可选传入前端生成的BPMN XML，未传则由后端根据已保存的图形数据生成）")
    public ApiResult<Boolean> deploy(@RequestBody ProcessDefinitionVo processDefinitionVo) {
        return ApiResult.success();
    }

    @GetMapping("/versions")
    @Schema(description = "获取BPMN XML列表（已部署的版本）")
    public ApiResult<Boolean> versions(@RequestParam("key") String key) {
        return ApiResult.success();
    }

    /**
     * 将LogicFlow流程图数据转换为BPMN 2.0 XML
     */
    @PostMapping("/convertToBpmn")
    @Schema(description = "将LogicFlow格式的流程图数据转换为Flowable 8.0 BPMN 2.0 XML格式")
    public ApiResult<String> convertToBpmn(@RequestBody FlowGraphData flowGraphData) {
        try {
            log.info("验证流程图数据开始");
            boolean isValid = flowConversionService.validateFlowGraphData(flowGraphData);
            log.info("验证流程图数据结束,结果:{}", isValid);
            if (!isValid) {
                return ApiResult.fail("流程图数据无效");
            }
            log.info("转换流程图数据开始");
            String bpmnXml = flowConversionService.convertToBpmnXml(flowGraphData);
            log.info("转换流程图数据结束 结果:{}", bpmnXml);


            return ApiResult.success(bpmnXml);
        } catch (Exception e) {
            log.error("转换流程图数据时出错", e);
            return ApiResult.fail("转换失败: " + e.getMessage());
        }
    }

    /**
     * 验证流程图数据有效性
     */
    @PostMapping("/validateFlowGraph")
    @Schema(description = "验证LogicFlow流程图数据的有效性")
    public ApiResult<Boolean> validateFlowGraph(@RequestBody FlowGraphData flowGraphData) {
        try {
            log.info("验证流程图数据开始");
            boolean isValid = flowConversionService.validateFlowGraphData(flowGraphData);
            log.info("验证流程图数据结束,结果:{}", isValid);
            return ApiResult.success(isValid);
        } catch (Exception e) {
            log.error("验证流程图数据时出错", e);
            return ApiResult.fail("验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的节点类型
     */
    @GetMapping("/supportedNodeTypes")
    @Schema(description = "获取适配器支持的所有LogicFlow节点类型")
    public ApiResult<String[]> getSupportedNodeTypes() {
        try {
            log.info("获取支持的节点类型开始");
            String[] nodeTypes = flowConversionService.getSupportedNodeTypes();
            log.info("获取支持的节点类型结束");
            return ApiResult.success(nodeTypes);
        } catch (Exception e) {
            log.error("获取支持的节点类型时出错", e);
            return ApiResult.fail("获取支持的节点类型失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的连线类型
     */
    @GetMapping("/supportedEdgeTypes")
    @Schema(description = "获取适配器支持的所有连线类型")
    public ApiResult<String[]> getSupportedEdgeTypes() {
        try {
            log.info("获取支持的连线类型开始");
            String[] edgeTypes = flowConversionService.getSupportedEdgeTypes();
            log.info("获取支持的连线类型结束");
            return ApiResult.success(edgeTypes);
        } catch (Exception e) {
            log.error("获取支持的连线类型时出错", e);
            return ApiResult.fail("获取支持的连线类型失败: " + e.getMessage());
        }
    }

    /**
     * 获取适配器信息
     */
    @GetMapping("/adapterInfo")
    @Schema(description = "获取适配器信息")
    public ApiResult<Map<String, String>> getAdapterInfo() {
        try {
            Map<String, String> adapterInfo = new HashMap<>();
            adapterInfo.put("name", flowConversionService.getAdapterName());
            adapterInfo.put("version", flowConversionService.getAdapterVersion());
            log.info("获取适配器信息:{}", adapterInfo);
            return ApiResult.success(adapterInfo);
        } catch (Exception e) {
            log.error("获取适配器信息时出错", e);
            return ApiResult.fail("获取适配器信息失败: " + e.getMessage());
        }
    }
}
