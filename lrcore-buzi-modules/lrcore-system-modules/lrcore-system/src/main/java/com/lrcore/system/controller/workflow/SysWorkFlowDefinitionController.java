package com.lrcore.system.controller.workflow;

import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.utils.jackson.FunJsonUtils;
import com.lrcore.common.core.web.controller.BaseController;
import com.lrcore.common.core.web.domain.ApiResult;
import com.lrcore.common.flowable.enums.ProcessDefinitionStatus;
import com.lrcore.common.flowable.model.definition.FlowGraphData;
import com.lrcore.common.flowable.model.definition.ProcessDefinitionVo;
import com.lrcore.common.flowable.model.definition.QueryDefinitionVi;
import com.lrcore.common.flowable.service.FlowConversionService;
import com.lrcore.system.domain.SysProcessDefinitionBaseInfoEntity;
import com.lrcore.system.domain.apt.SysProcessDefinitionBaseInfoAPT;
import com.lrcore.system.service.ISysProcessDefinitionBaseInfoService;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.transaction.annotation.Transactional;
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
    private final ISysProcessDefinitionBaseInfoService sysProcessDefinitionBaseInfoService;

    @GetMapping("/list")
    @Schema(description = "获取流程定义列表")
    public ApiResult<List<ProcessDefinitionVo>> page(QueryDefinitionVi queryDefinitionVi) {
        String keyword = queryDefinitionVi != null ? queryDefinitionVi.getKeyword() : null;
        ProcessDefinitionStatus status = parseStatus(queryDefinitionVi != null ? queryDefinitionVi.getStatus() : null);

        // 主表 t_process_definition_base_info：deleted=0、关键字/状态过滤、按更新时间倒序
        List<SysProcessDefinitionBaseInfoEntity> entities =
                sysProcessDefinitionBaseInfoService.listDefinitions(keyword, status);
        if (entities == null || entities.isEmpty()) {
            return ApiResult.success(java.util.Collections.emptyList());
        }

        // 附表 Flowable 流程定义（act_re_procdef）：按 act_re_procdef_id 批量查询，补充引擎侧流程版本
        java.util.Set<String> definitionIds = entities.stream()
                .map(SysProcessDefinitionBaseInfoEntity::getActReProcdefId)
                .filter(FunStrUtils::hasText)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        Map<String, ProcessDefinition> definitionMap = loadDefinitionMap(definitionIds);

        List<ProcessDefinitionVo> list = entities.stream()
                .map(entity -> convertToVo(entity,
                        entity.getActReProcdefId() != null ? definitionMap.get(entity.getActReProcdefId()) : null))
                .collect(Collectors.toList());
        return ApiResult.success(list);
    }

    /**
     * 状态参数解析：空表示不过滤；非法值忽略过滤并告警，避免 valueOf 抛异常导致 500
     */
    private ProcessDefinitionStatus parseStatus(String status) {
        if (!FunStrUtils.hasText(status)) {
            return null;
        }
        try {
            return ProcessDefinitionStatus.valueOf(status.trim());
        } catch (IllegalArgumentException e) {
            log.warn("非法的流程定义状态参数，忽略状态过滤: {}", status);
            return null;
        }
    }

    /**
     * 批量查询附表（Flowable 流程定义），返回 定义ID -> ProcessDefinition 映射
     */
    private Map<String, ProcessDefinition> loadDefinitionMap(java.util.Set<String> definitionIds) {
        if (definitionIds == null || definitionIds.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionIds(definitionIds)
                .list()
                .stream()
                .collect(Collectors.toMap(ProcessDefinition::getId, java.util.function.Function.identity(), (a, b) -> a));
    }

    /**
     * 主表实体 → 流程定义 VO：
     * <ul>
     *     <li>业务字段（名称/Key/描述/分类/状态/时间）以主表为准；</li>
     *     <li>状态为主表生命周期状态（draft/deployed/archived），deleted=0 过滤已在查询层完成；</li>
     *     <li>版本优先取附表（Flowable）引擎侧流程版本；主表 version 列存储引擎版本号，
     *     仅当为纯数字时（未部署草稿）兜底解析；</li>
     *     <li>id 取 act_re_procdef_id，与 getInfo/deploy 接口的 Flowable 定义ID 契约一致；</li>
     *     <li>列表不返回 bpmnXml/graphData 大字段，由详情接口 getInfo 提供。</li>
     * </ul>
     */
    private ProcessDefinitionVo convertToVo(SysProcessDefinitionBaseInfoEntity entity, ProcessDefinition processDefinition) {
        ProcessDefinitionVo vo = new ProcessDefinitionVo();
        vo.setId(entity.getId());
        vo.setActReProcdefId(entity.getActReProcdefId());
        vo.setKey(entity.getKey());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setCategory(entity.getCategory());
        // 版本优先取附表（Flowable）引擎侧流程版本，未部署草稿用主表版本兜底
        //vo.setVersion(processDefinition != null ? processDefinition.getVersion() : parseNumericVersion(entity.getVersion()));
        vo.setStatus(entity.getStatus());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    /**
     * 解析纯数字版本串，非数字（如引擎版本号）返回 null
     */
    private Integer parseNumericVersion(String version) {
        if (!FunStrUtils.hasText(version)) {
            return null;
        }
        try {
            return Integer.valueOf(version.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @GetMapping("/getInfo/{id}")
    @Schema(description = "获取流程定义详情")
    public ApiResult<ProcessDefinitionVo> getInfo(@PathVariable("id") Serializable id) {
        // 主表优先（id 为主表主键），附表（Flowable）仅用于补充引擎侧信息
        SysProcessDefinitionBaseInfoEntity entity = sysProcessDefinitionBaseInfoService.getById(id);
        if (entity == null) {
            log.warn("流程定义不存在, id:{}", id);
            return ApiResult.fail("流程定义不存在: " + id);
        }

        ProcessDefinition processDefinition = FunStrUtils.hasText(entity.getActReProcdefId())
                ? repositoryService.createProcessDefinitionQuery()
                        .processDefinitionId(entity.getActReProcdefId())
                        .singleResult()
                : null;

        ProcessDefinitionVo vo = convertToVo(entity, processDefinition);

        // 流程图数据：主表保存的 LogicFlow 原始数据，设计器据此无损还原画布
        if (FunStrUtils.hasText(entity.getGraphData())) {
            vo.setGraphData(FunJsonUtils.getJavaBeanFromJsonStr(entity.getGraphData(), FlowGraphData.class));
        }
        // BPMN XML：优先主表存储，缺失时从部署资源读取
        if (FunStrUtils.hasText(entity.getBpmnXml())) {
            vo.setBpmnXml(entity.getBpmnXml());
        } else if (processDefinition != null) {
            vo.setBpmnXml(loadBpmnXml(processDefinition));
        }
        return ApiResult.success(vo);
    }

    /**
     * 从部署资源中读取流程定义的 BPMN XML，读取失败时返回 null（不阻断基本信息返回）
     */
    private String loadBpmnXml(ProcessDefinition processDefinition) {
        try (java.io.InputStream in = repositoryService.getResourceAsStream(
                processDefinition.getId(), processDefinition.getResourceName())) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("读取流程定义BPMN资源失败, id:{}, resource:{}",
                    processDefinition.getId(), processDefinition.getResourceName(), e);
            return null;
        }
    }

    @PostMapping("/save")
    @Schema(description = "创建流程定义")
    public ApiResult<ProcessDefinitionVo> processDefinitionsSave(@RequestBody ProcessDefinitionVo processDefinitionVo) {
        log.info("创建流程定义");
        try {
            long count = sysProcessDefinitionBaseInfoService.count(QueryWrapper.create()
                    .where(SysProcessDefinitionBaseInfoAPT.SYS_PROCESS_DEFINITION_BASE_INFO.KEY.eq(processDefinitionVo.getKey()))
                    .and(SysProcessDefinitionBaseInfoAPT.SYS_PROCESS_DEFINITION_BASE_INFO.DELETED.eq(0)));
            if (count > 0) {
                throw new ServiceException("当前流程key:" + processDefinitionVo.getKey() + "已经被创建了， 请检查！");
            }
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
            processDefinitionVo.setActReProcdefId(deployment.getId());
            processDefinitionVo.setBpmnXml(bpmnXml);
            processDefinitionVo.setCreateTime(java.time.LocalDateTime.now());
            processDefinitionVo.setUpdateTime(java.time.LocalDateTime.now());
            SysProcessDefinitionBaseInfoEntity processDefinitionBaseInfoEntity = SysProcessDefinitionBaseInfoEntity.builder()
                    .actReProcdefId(deployment.getId())
                    .key(deployment.getKey())
                    .name(deployment.getName())
                    .category(deployment.getCategory())
                    .description(processDefinitionVo.getDescription())
                    .version(deployment.getEngineVersion())
                    .graphData(FunJsonUtils.getJsonStringFromJavaBean(processDefinitionVo.getGraphData()))
                    .bpmnXml(bpmnXml)
                    .status(ProcessDefinitionStatus.deployed)
                    .buildIn(0)
                    .build();
            sysProcessDefinitionBaseInfoService.save(processDefinitionBaseInfoEntity);
            // 回传主表ID，前端后续编辑/部署/删除均以此定位
            processDefinitionVo.setId(processDefinitionBaseInfoEntity.getId());
            processDefinitionVo.setStatus(ProcessDefinitionStatus.deployed);

            return ApiResult.success(processDefinitionVo);
        } catch (ServiceException e) {
            log.warn("创建流程定义失败: {}", e.getErrorMessage());
            return ApiResult.fail("创建流程定义失败: " + e.getErrorMessage());
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
    @Schema(description = "删除流程定义（先删 Flowable 侧定义，成功后逻辑删除主表记录）")
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> processDefinitionsDelete(@PathVariable("id") Long id) {
        SysProcessDefinitionBaseInfoEntity entity = sysProcessDefinitionBaseInfoService.getById(id);
        if (entity == null) {
            log.warn("流程定义不存在，无法删除, id:{}", id);
            return ApiResult.fail("流程定义不存在: " + id);
        }

        // 1. 先删除流程定义附表（Flowable）：删除部署会同时移除其下的流程定义与资源（BPMN 等）；
        //    默认非级联删除，部署下存在进行中的流程实例时引擎抛异常，此时不触碰主表
        if (FunStrUtils.hasText(entity.getActReProcdefId())) {
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(entity.getActReProcdefId())
                    .singleResult();
            if (processDefinition != null) {
                try {
                    repositoryService.deleteDeployment(processDefinition.getDeploymentId());
                    log.info("Flowable侧流程定义已删除, definitionId:{}, deploymentId:{}",
                            processDefinition.getId(), processDefinition.getDeploymentId());
                } catch (Exception e) {
                    log.warn("删除Flowable侧流程定义失败（可能存在进行中的流程实例）, actReProcdefId:{}",
                            entity.getActReProcdefId(), e);
                    return ApiResult.fail("删除流程定义失败: " + e.getMessage());
                }
            } else {
                log.warn("主表记录的Flowable流程定义已不存在，仅逻辑删除主表, id:{}", id);
            }
        }

        // 2. 附表删除成功之后，再删除主表（逻辑删除）
        sysProcessDefinitionBaseInfoService.logicDeleteById(id);
        log.info("流程定义已逻辑删除, id:{}", id);
        return ApiResult.success(true);
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
    @Schema(description = "部署流程（按主表ID定位；未传 BPMN XML 时用主表已保存的图数据生成；成功后主表回写新版本并置为已部署）")
    public ApiResult<Boolean> deploy(@RequestBody ProcessDefinitionVo processDefinitionVo) {
        if (processDefinitionVo == null || processDefinitionVo.getId() == null) {
            return ApiResult.fail("流程定义ID不能为空");
        }
        SysProcessDefinitionBaseInfoEntity entity = sysProcessDefinitionBaseInfoService.getById(processDefinitionVo.getId());
        if (entity == null) {
            return ApiResult.fail("流程定义不存在: " + processDefinitionVo.getId());
        }

        String bpmnXml = processDefinitionVo.getBpmnXml();
        // 列表页「部署」场景：前端只传ID，用主表已保存的流程图数据生成 BPMN
        if (FunStrUtils.isEmpty(bpmnXml) && FunStrUtils.hasText(entity.getGraphData())) {
            FlowGraphData graphData = FunJsonUtils.getJavaBeanFromJsonStr(entity.getGraphData(), FlowGraphData.class);
            bpmnXml = flowConversionService.convertToBpmnXml(graphData);
        }
        if (FunStrUtils.isEmpty(bpmnXml)) {
            return ApiResult.fail("BPMN XML不能为空");
        }

        Deployment deployment = repositoryService.createDeployment()
                .key(entity.getKey())
                .name(entity.getName())
                .category(entity.getCategory())
                .addInputStream(entity.getKey() + ".bpmn20.xml",
                        new java.io.ByteArrayInputStream(bpmnXml.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .deploy();

        // 状态管理：回写引擎侧新版本定义ID，主表状态 draft -> deployed
        ProcessDefinition newDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(entity.getKey())
                .latestVersion()
                .singleResult();
        if (newDefinition != null) {
            sysProcessDefinitionBaseInfoService.markDeployed(entity.getId(), newDefinition.getId());
        }
        log.info("流程部署成功, baseInfoId:{}, deploymentId:{}, key:{}",
                entity.getId(), deployment.getId(), entity.getKey());
        return ApiResult.success(true);
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
