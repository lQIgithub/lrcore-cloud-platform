package com.lrcore.system.service.workflow.impl;

import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.common.flowable.enums.ProcessInstanceStatus;
import com.lrcore.common.flowable.model.instance.ProcessInstanceVO;
import com.lrcore.common.flowable.model.instance.StartInstanceVi;
import com.lrcore.system.enums.LeaveStatus;
import com.lrcore.system.service.ILeaveApplicationService;
import com.lrcore.system.service.workflow.IWorkflowInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程实例 服务层实现（启动/查询/变量/终止/挂起激活）
 * @ClassName: WorkflowInstanceServiceImpl
 * @Author: lrcore
 * @Date: 2026/09/02
 * @Version: 1.0
 *
 * <p>事务说明：Flowable 引擎独立事务，Spring @Transactional 不覆盖；
 * 采用"本地保存 → 引擎启动 → 回写+失败补偿删孤儿"保证近似原子。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowInstanceServiceImpl implements IWorkflowInstanceService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    /** 请假流程：申请人提交节点Key（启动后自动完成，直达审批节点） */
    private static final String NODE_APPLY = "node_apply";

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;
    private final TaskService taskService;
    private final ILeaveApplicationService leaveApplicationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstanceVO start(StartInstanceVi vi) {
        if (vi == null || !FunStrUtils.hasText(vi.getKey())) {
            throw new ServiceException("流程定义Key不能为空");
        }
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(vi.getKey())
                .latestVersion()
                .active()
                .singleResult();
        if (definition == null) {
            throw new ServiceException("流程定义不存在或未激活：" + vi.getKey());
        }
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new ServiceException("未获取到当前登录用户");
        }

        Map<String, Object> variables = vi.getVariables() != null
                ? new HashMap<>(vi.getVariables())
                : new HashMap<>();
        variables.putIfAbsent("applyUser", String.valueOf(userId));
        if (variables.get("manager") == null) {
            throw new ServiceException("请指定审批人（variables.manager）");
        }
        String businessKey = FunStrUtils.hasText(vi.getBusinessKey())
                ? vi.getBusinessKey()
                : "LEAVE-" + System.currentTimeMillis();
        variables.put("businessKey", businessKey);

        // 1. 本地落库（Spring 事务内，失败可整体回滚）
        leaveApplicationService.buildAndSave(businessKey, userId, variables);

        // 2. Flowable 独立事务启动（设置认证用户记录 startUserId，SSE 由引擎事件监听器推送）
        ProcessInstance instance;
        Authentication.setAuthenticatedUserId(String.valueOf(userId));
        try {
            instance = runtimeService.startProcessInstanceByKey(vi.getKey(), businessKey, variables);
        } finally {
            // 清理线程上下文，避免影响同一线程后续 Flowable 操作
            Authentication.setAuthenticatedUserId(null);
        }

        // 3. 回写流程实例ID，失败则补偿删除孤儿实例
        try {
            leaveApplicationService.bindProcessInstance(businessKey, instance.getId());
        } catch (Exception e) {
            log.error("请假记录回写流程实例ID失败，补偿删除孤儿实例 processInstanceId={}", instance.getId(), e);
            runtimeService.deleteProcessInstance(instance.getId(), "请假记录回写失败补偿删除");
            throw new ServiceException("启动流程后业务回写失败：" + e.getMessage());
        }

        // 4. 自动完成"填写请假申请"节点：表单提交即视为完成填写步骤，
        //    使流程直达"经理审批"节点，打通"提交 → 审批"主链路
        completeApplyNode(instance.getId());

        log.info("请假流程启动成功 businessKey={}, processInstanceId={}", businessKey, instance.getId());
        return toVo(instance, definition);
    }

    /**
     * 自动完成"填写请假申请"节点（node_apply），将流程推进到审批节点（node_approve）。
     */
    private void completeApplyNode(String processInstanceId) {
        Task applyTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .taskDefinitionKey(NODE_APPLY)
                .singleResult();
        if (applyTask == null) {
            log.warn("未找到填写请假申请节点任务，流程可能已异常, processInstanceId={}", processInstanceId);
            return;
        }
        Authentication.setAuthenticatedUserId(applyTask.getAssignee());
        try {
            taskService.complete(applyTask.getId());
        } finally {
            Authentication.setAuthenticatedUserId(null);
        }
        log.info("自动完成填写请假申请节点 taskId={}, assignee={}", applyTask.getId(), applyTask.getAssignee());
    }

    @Override
    public Map<String, Object> list(String processDefinitionKey, String businessKey, String status,
                                    Long pageNum, Long pageSize) {
        Long userId = requireUserId();
        int pageNo = pageNum != null && pageNum > 0 ? pageNum.intValue() : DEFAULT_PAGE_NUM;
        int pageSizeNo = pageSize != null && pageSize > 0 ? pageSize.intValue() : DEFAULT_PAGE_SIZE;

        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                // startedBy 依赖 Flowable 认证用户，可能为空；改用 applyUser 流程变量过滤更可靠
                .variableValueEquals("applyUser", String.valueOf(userId));
        if (FunStrUtils.hasText(processDefinitionKey)) {
            query.processDefinitionKey(processDefinitionKey);
        }
        if (FunStrUtils.hasText(businessKey)) {
            query.processInstanceBusinessKey(businessKey);
        }
        query.orderByProcessInstanceStartTime().desc();

        List<HistoricProcessInstance> instances;
        long total;
        if (FunStrUtils.hasText(status)) {
            // 历史查询不支持按运行状态过滤，取全量后内存过滤（本期数据量小）
            List<HistoricProcessInstance> all = query.list();
            List<HistoricProcessInstance> filtered = new ArrayList<>();
            for (HistoricProcessInstance hpi : all) {
                if (status.equals(statusOf(hpi).name())) {
                    filtered.add(hpi);
                }
            }
            total = filtered.size();
            int from = Math.min(pageNo - 1, filtered.size());
            int to = Math.min(from + pageSizeNo, filtered.size());
            instances = new ArrayList<>(filtered.subList(from, to));
        } else {
            total = query.count();
            int first = (pageNo - 1) * pageSizeNo;
            instances = query.listPage(first, pageSizeNo);
        }

        List<ProcessInstanceVO> voList = new ArrayList<>();
        for (HistoricProcessInstance hpi : instances) {
            voList.add(buildFromHistoric(hpi));
        }
        return pageResult(voList, total);
    }

    @Override
    public ProcessInstanceVO getInfo(String id) {
        HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(id)
                .singleResult();
        if (hpi == null) {
            throw new ServiceException("流程实例不存在：" + id);
        }
        return buildFromHistoric(hpi);
    }

    @Override
    public Map<String, Object> getVariables(String id) {
        return loadVariables(id);
    }

    @Override
    public void setVariables(String id, Map<String, Object> variables) {
        runtimeService.setVariables(id, variables != null ? variables : Map.of());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String id) {
        runtimeService.deleteProcessInstance(id, "用户终止流程实例");
        historyService.deleteHistoricProcessInstance(id);
        leaveApplicationService.markStatusByInstance(id, LeaveStatus.cancelled);
        log.info("流程实例已终止删除 processInstanceId={}", id);
    }

    @Override
    public void suspend(String id) {
        runtimeService.suspendProcessInstanceById(id);
    }

    @Override
    public void activate(String id) {
        runtimeService.activateProcessInstanceById(id);
    }

    /**
     * 组装流程实例VO（从引擎运行时实例）
     */
    private ProcessInstanceVO toVo(ProcessInstance instance, ProcessDefinition definition) {
        ProcessInstanceVO vo = new ProcessInstanceVO();
        vo.setId(instance.getId());
        vo.setProcessDefinitionId(instance.getProcessDefinitionId());
        vo.setProcessDefinitionKey(instance.getProcessDefinitionKey());
        vo.setProcessDefinitionName(definition != null ? definition.getName() : instance.getProcessDefinitionName());
        vo.setBusinessKey(instance.getBusinessKey());
        vo.setStatus(ProcessInstanceStatus.active);
        vo.setStartTime(toLocalDateTime(instance.getStartTime()));
        return vo;
    }

    /**
     * 组装流程实例VO（从历史实例，含状态/结束时间/变量）
     */
    private ProcessInstanceVO buildFromHistoric(HistoricProcessInstance hpi) {
        ProcessInstanceVO vo = new ProcessInstanceVO();
        vo.setId(hpi.getId());
        vo.setProcessDefinitionId(hpi.getProcessDefinitionId());
        vo.setProcessDefinitionKey(hpi.getProcessDefinitionKey());
        vo.setProcessDefinitionName(resolveDefinitionName(hpi.getProcessDefinitionId(), hpi.getProcessDefinitionName()));
        vo.setBusinessKey(hpi.getBusinessKey());
        vo.setStatus(statusOf(hpi));
        vo.setStartTime(toLocalDateTime(hpi.getStartTime()));
        vo.setEndTime(toLocalDateTime(hpi.getEndTime()));
        vo.setVariables(loadVariables(hpi.getId()));
        return vo;
    }

    /**
     * 历史实例状态：运行中（active/suspended）按运行时实例判断，已结束按删除原因区分
     */
    private ProcessInstanceStatus statusOf(HistoricProcessInstance hpi) {
        if (hpi.getEndTime() == null) {
            ProcessInstance running = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(hpi.getId())
                    .singleResult();
            if (running != null) {
                return running.isSuspended() ? ProcessInstanceStatus.suspended : ProcessInstanceStatus.active;
            }
            return ProcessInstanceStatus.active;
        }
        return FunStrUtils.hasText(hpi.getDeleteReason())
                ? ProcessInstanceStatus.terminated
                : ProcessInstanceStatus.completed;
    }

    /**
     * 解析流程定义名称（历史实例只存名称快照，必要时回查定义表）
     */
    private String resolveDefinitionName(String processDefinitionId, String fallbackName) {
        if (!FunStrUtils.hasText(processDefinitionId)) {
            return fallbackName;
        }
        try {
            ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();
            if (definition != null && FunStrUtils.hasText(definition.getName())) {
                return definition.getName();
            }
        } catch (Exception e) {
            log.debug("查询流程定义名称失败，使用历史快照 processDefinitionId={}", processDefinitionId);
        }
        return fallbackName;
    }

    /**
     * 加载流程实例历史变量
     */
    private Map<String, Object> loadVariables(String processInstanceId) {
        List<HistoricVariableInstance> variables = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .list();
        Map<String, Object> map = new LinkedHashMap<>();
        if (variables != null) {
            for (HistoricVariableInstance variable : variables) {
                map.put(variable.getVariableName(), variable.getValue());
            }
        }
        return map;
    }

    private Long requireUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new ServiceException("未获取到当前登录用户");
        }
        return userId;
    }

    /**
     * java.util.Date → LocalDateTime（引擎时间为 UTC 毫秒，直接转换即可）
     */
    private java.time.LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null
                : java.time.LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

    private Map<String, Object> pageResult(List<?> list, long total) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        return result;
    }

}
