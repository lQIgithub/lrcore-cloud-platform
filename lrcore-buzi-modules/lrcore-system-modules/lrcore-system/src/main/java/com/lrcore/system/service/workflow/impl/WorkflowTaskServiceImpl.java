package com.lrcore.system.service.workflow.impl;

import com.lrcore.common.core.exception.ServiceException;
import com.lrcore.common.core.utils.FunStrUtils;
import com.lrcore.common.core.utils.SecurityUtils;
import com.lrcore.common.flowable.enums.ProcessTaskStatus;
import com.lrcore.common.flowable.model.task.ProcessTaskVO;
import com.lrcore.system.enums.LeaveStatus;
import com.lrcore.system.service.ILeaveApplicationService;
import com.lrcore.system.service.workflow.IWorkflowTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.FlowNode;
import org.flowable.bpmn.model.SequenceFlow;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 流程任务 服务层实现（待办/详情/签收/通过/驳回）
 * @ClassName: WorkflowTaskServiceImpl
 * @Author: lrcore
 * @Date: 2026/09/02
 * @Version: 1.0
 *
 * <p>驳回说明：基于 ChangeActivityStateBuilder 将当前任务移动回最近上游 UserTask，
 * 仅适用线性流程（请假流程为线性）。SSE 推送由引擎事件监听器自动完成。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowTaskServiceImpl implements IWorkflowTaskService {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;

    /** 请假流程：申请人提交节点Key */
    private static final String NODE_APPLY = "node_apply";

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final ILeaveApplicationService leaveApplicationService;

    @Override
    public Map<String, Object> list(String processInstanceId, String status, Long pageNum, Long pageSize) {
        Long userId = requireUserId();
        int pageNo = pageNum != null && pageNum > 0 ? pageNum.intValue() : DEFAULT_PAGE_NUM;
        int pageSizeNo = pageSize != null && pageSize > 0 ? pageSize.intValue() : DEFAULT_PAGE_SIZE;

        TaskQuery query = taskService.createTaskQuery()
                .taskCandidateOrAssigned(String.valueOf(userId))
                .active();
        if (FunStrUtils.hasText(processInstanceId)) {
            query.processInstanceId(processInstanceId);
        }
        long total = query.count();
        int first = (pageNo - 1) * pageSizeNo;
        List<Task> tasks = query.orderByTaskCreateTime().desc().listPage(first, pageSizeNo);

        List<ProcessTaskVO> voList = new ArrayList<>();
        for (Task task : tasks) {
            voList.add(toVo(task));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", voList);
        result.put("total", total);
        return result;
    }

    @Override
    public ProcessTaskVO getInfo(String id) {
        return toVo(requireTask(id));
    }

    @Override
    public void claim(String id) {
        Task task = requireTask(id);
        String userId = String.valueOf(requireUserId());
        if (task.getAssignee() != null && !task.getAssignee().equals(userId)) {
            throw new ServiceException("任务已被他人签收");
        }
        taskService.claim(id, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(String id, Map<String, Object> variables) {
        Task task = requireTask(id);
        checkAssignee(task);
        Map<String, Object> vars = variables != null ? new HashMap<>(variables) : new HashMap<>();
        String taskDefinitionKey = task.getTaskDefinitionKey();
        taskService.complete(id, vars);
        syncLeaveStatusByTask(task, taskDefinitionKey, vars);
        log.info("任务已完成 taskId={}, taskDefinitionKey={}", id, taskDefinitionKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(String id, String comment) {
        Task task = requireTask(id);
        checkAssignee(task);
        BpmnModel model = repositoryService.getBpmnModel(task.getProcessDefinitionId());
        String targetKey = findUpstreamUserTaskKey(model, task.getTaskDefinitionKey());
        if (targetKey == null) {
            throw new ServiceException("未找到可回退的上游节点，无法驳回");
        }
        taskService.addComment(task.getId(), task.getProcessInstanceId(), comment);
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveActivityIdTo(task.getTaskDefinitionKey(), targetKey)
                .processVariables(Map.of("approved", false, "rejectComment", comment == null ? "" : comment))
                .changeState();
        leaveApplicationService.markStatusByInstance(task.getProcessInstanceId(), LeaveStatus.rejected);
        log.info("任务已驳回 taskId={}, 回退至节点={}", id, targetKey);
    }

    @Override
    public void transfer(String id, String targetUserId) {
        throw new ServiceException("转办功能暂未开放");
    }

    /**
     * 完成任务后同步请假业务状态
     */
    private void syncLeaveStatusByTask(Task task, String taskDefinitionKey, Map<String, Object> vars) {
        String processInstanceId = task.getProcessInstanceId();
        boolean approved = Boolean.parseBoolean(String.valueOf(vars.get("approved")));
        if (NODE_APPLY.equals(taskDefinitionKey)) {
            // 申请人提交 → 待审批
            leaveApplicationService.markStatusByInstance(processInstanceId, LeaveStatus.pending);
        } else if (approved) {
            // 审批通过：实例已结束 → approved，仍在跑 → completed
            boolean running = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .count() > 0;
            leaveApplicationService.markStatusByInstance(processInstanceId,
                    running ? LeaveStatus.completed : LeaveStatus.approved);
        } else {
            // 审批不通过（approved=false）→ 驳回
            leaveApplicationService.markStatusByInstance(processInstanceId, LeaveStatus.rejected);
        }
    }

    /**
     * 逆向查找最近上游 UserTask Key（线性流程；网关沿 incoming 继续，StartEvent 返回 null）
     */
    private String findUpstreamUserTaskKey(BpmnModel model, String taskKey) {
        FlowElement element = model.getFlowElement(taskKey);
        if (element == null) {
            return null;
        }
        // 从当前节点的上游流开始回溯（跳过当前节点自身，避免把当前任务当回退目标）
        if (element instanceof FlowNode node) {
            for (SequenceFlow flow : node.getIncomingFlows()) {
                String key = resolveUpstreamUserTask(flow.getSourceFlowElement(), new HashSet<>());
                if (key != null) {
                    return key;
                }
            }
        }
        return null;
    }

    private String resolveUpstreamUserTask(FlowElement element, Set<String> visited) {
        if (element == null || !visited.add(element.getId())) {
            return null;
        }
        if (element instanceof UserTask) {
            return element.getId();
        }
        if (element instanceof FlowNode node) {
            for (SequenceFlow flow : node.getIncomingFlows()) {
                String key = resolveUpstreamUserTask(flow.getSourceFlowElement(), visited);
                if (key != null) {
                    return key;
                }
            }
        }
        return null;
    }

    private ProcessTaskVO toVo(Task task) {
        ProcessTaskVO vo = new ProcessTaskVO();
        vo.setId(task.getId());
        vo.setName(task.getName());
        vo.setAssignee(task.getAssignee());
        vo.setProcessInstanceId(task.getProcessInstanceId());
        vo.setCreateTime(toLocalDateTime(task.getCreateTime()));
        vo.setDueDate(toLocalDateTime(task.getDueDate()));
        vo.setPriority(task.getPriority());
        vo.setStatus(task.getAssignee() != null ? ProcessTaskStatus.claimed : ProcessTaskStatus.pending);
        return vo;
    }

    private Task requireTask(String id) {
        Task task = taskService.createTaskQuery().taskId(id).singleResult();
        if (task == null) {
            throw new ServiceException("任务不存在或已处理：" + id);
        }
        return task;
    }

    /**
     * 越权校验：当前用户须为该任务处理人
     */
    private void checkAssignee(Task task) {
        String userId = String.valueOf(requireUserId());
        if (task.getAssignee() == null || !task.getAssignee().equals(userId)) {
            throw new ServiceException("无权处理该任务（仅限当前处理人）");
        }
    }

    private Long requireUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new ServiceException("未获取到当前登录用户");
        }
        return userId;
    }

    /**
     * java.util.Date → LocalDateTime
     */
    private java.time.LocalDateTime toLocalDateTime(Date date) {
        return date == null ? null
                : java.time.LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
    }

}
