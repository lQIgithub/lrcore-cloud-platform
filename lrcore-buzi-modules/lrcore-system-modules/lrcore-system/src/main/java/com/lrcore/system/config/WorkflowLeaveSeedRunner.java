package com.lrcore.system.config;

import com.lrcore.common.flowable.enums.ProcessDefinitionStatus;
import com.lrcore.system.domain.SysProcessDefinitionBaseInfoEntity;
import com.lrcore.system.service.ISysProcessDefinitionBaseInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 请假流程定义 seed：启动时若 key='leave' 未部署，则从 classpath BPMN 部署并回写主表。
 *
 * <p>保证端到端主链路可复现；重复启动自动跳过。流程设计：</p>
 * <pre>
 * 开始 → 填写请假申请(assignee=${applyUser}) → 经理审批(assignee=${manager})
 *      → 排他网关(approved==true) → 结束
 * </pre>
 * 驳回由任务服务基于 ChangeActivityStateBuilder 回退实现，无需预置回退边。
 *
 * @ClassName: WorkflowLeaveSeedRunner
 * @Author: lrcore
 * @Date: 2026/09/02
 * @Version: 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowLeaveSeedRunner implements CommandLineRunner {

    private static final String LEAVE_KEY = "leave";
    private static final String LEAVE_NAME = "公司请假流程";
    private static final String BPMN_PATH = "bpmn/leave.bpmn20.xml";

    private final RepositoryService repositoryService;
    private final ISysProcessDefinitionBaseInfoService sysProcessDefinitionBaseInfoService;

    @Override
    public void run(String... args) {
        try {
            boolean deployed = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(LEAVE_KEY)
                    .count() > 0;
            if (deployed) {
                log.info("请假流程已部署，跳过 seed（key={}）", LEAVE_KEY);
                return;
            }

            Deployment deployment = repositoryService.createDeployment()
                    .key(LEAVE_KEY)
                    .name(LEAVE_NAME)
                    .addInputStream("leave.bpmn20.xml",
                            new ClassPathResource(BPMN_PATH).getInputStream())
                    .deploy();
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionKey(LEAVE_KEY)
                    .latestVersion()
                    .singleResult();
            log.info("请假流程 seed 部署成功 deploymentId={}, definitionId={}",
                    deployment.getId(), definition != null ? definition.getId() : null);
            if (definition != null) {
                upsertBaseInfo(definition);
            }
        } catch (Exception e) {
            log.error("请假流程 seed 执行失败（不影响其他启动流程）", e);
        }
    }

    /**
     * 回写/新建流程定义主表记录（key=leave），失败仅告警不阻断部署
     */
    private void upsertBaseInfo(ProcessDefinition definition) {
        try {
            List<SysProcessDefinitionBaseInfoEntity> exists = sysProcessDefinitionBaseInfoService
                    .listDefinitions(LEAVE_KEY, null);
            SysProcessDefinitionBaseInfoEntity entity = exists.stream()
                    .filter(item -> LEAVE_KEY.equals(item.getKey()))
                    .findFirst()
                    .orElseGet(SysProcessDefinitionBaseInfoEntity::new);

            entity.setKey(LEAVE_KEY);
            entity.setName(LEAVE_NAME);
            entity.setCategory("系统通用审批");
            entity.setDescription("请假申请与审批主链路（seed 内置）");
            entity.setVersion("1");
            entity.setActReProcdefId(definition.getId());
            entity.setStatus(ProcessDefinitionStatus.deployed);
            entity.setBuildIn(1);
            entity.setDeleted(0);

            if (entity.getId() == null) {
                sysProcessDefinitionBaseInfoService.save(entity);
            } else {
                sysProcessDefinitionBaseInfoService.updateById(entity);
            }
            log.info("请假流程主表信息已回写 baseInfoId={}, actReProcdefId={}", entity.getId(), definition.getId());
        } catch (Exception e) {
            log.warn("请假流程主表回写失败（流程定义仍可部署启动）", e);
        }
    }

}
