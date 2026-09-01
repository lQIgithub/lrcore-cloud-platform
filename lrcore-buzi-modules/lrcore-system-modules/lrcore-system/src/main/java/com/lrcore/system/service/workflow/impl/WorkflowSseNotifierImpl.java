package com.lrcore.system.service.workflow.impl;

import com.lrcore.system.service.sse.SseConstants;
import com.lrcore.system.service.sse.SsePublisher;
import com.lrcore.system.service.workflow.WorkflowSseNotifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 工作流事件 → SSE 推送默认实现。
 *
 * <p>事件载荷（JSON，ID 一律字符串防前端精度丢失）：
 * <pre>
 * workflow-task:     {type, taskId, processInstanceId, taskName, processDefinitionId, taskDefinitionKey, assigneeId, time}
 * workflow-instance: {type, processInstanceId, processDefinitionKey?, businessKey?, time}
 * </pre>
 * type 取值：
 * <ul>
 *   <li>workflow-task：created / assigned / completed / deleted</li>
 *   <li>workflow-instance：started / completed / suspended / activated / cancelled</li>
 * </ul>
 *
 * @Version: 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowSseNotifierImpl implements WorkflowSseNotifier {

    private final SsePublisher ssePublisher;

    @Override
    public void notifyTaskCreated(Task task) {
        publishTask("created", task);
    }

    @Override
    public void notifyTaskAssigned(Task task) {
        publishTask("assigned", task);
    }

    @Override
    public void notifyTaskCompleted(Task task) {
        publishTask("completed", task);
    }

    @Override
    public void notifyTaskDeleted(Task task) {
        publishTask("deleted", task);
    }

    @Override
    public void notifyInstanceStarted(String processInstanceId, String processDefinitionKey, String businessKey, Long initiatorId) {
        publishInstance("started", processInstanceId, processDefinitionKey, businessKey, initiatorId);
    }

    @Override
    public void notifyInstanceCompleted(String processInstanceId, Long initiatorId) {
        publishInstance("completed", processInstanceId, null, null, initiatorId);
    }

    @Override
    public void notifyInstanceSuspended(String processInstanceId, Long initiatorId) {
        publishInstance("suspended", processInstanceId, null, null, initiatorId);
    }

    @Override
    public void notifyInstanceActivated(String processInstanceId, Long initiatorId) {
        publishInstance("activated", processInstanceId, null, null, initiatorId);
    }

    @Override
    public void notifyInstanceCancelled(String processInstanceId, Long initiatorId) {
        publishInstance("cancelled", processInstanceId, null, null, initiatorId);
    }

    private void publishTask(String type, Task task) {
        if (task == null) {
            return;
        }
        Long assigneeId = parseUserId(task.getAssignee());
        if (assigneeId == null) {
            log.debug("跳过工作流任务 SSE 推送：任务无处理人（type={}, taskId={})", type, task.getId());
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", type);
        payload.put("taskId", task.getId());
        payload.put("processInstanceId", task.getProcessInstanceId());
        payload.put("taskName", task.getName());
        payload.put("processDefinitionId", task.getProcessDefinitionId());
        payload.put("taskDefinitionKey", task.getTaskDefinitionKey());
        payload.put("assigneeId", task.getAssignee());
        payload.put("time", System.currentTimeMillis());
        ssePublisher.sendToUser(assigneeId, SseConstants.EVENT_WORKFLOW_TASK, payload);
    }

    private void publishInstance(String type, String processInstanceId, String processDefinitionKey, String businessKey, Long initiatorId) {
        if (initiatorId == null || processInstanceId == null) {
            log.debug("跳过工作流实例 SSE 推送：缺少发起人或实例ID（type={}, instanceId={})", type, processInstanceId);
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", type);
        payload.put("processInstanceId", processInstanceId);
        if (processDefinitionKey != null) {
            payload.put("processDefinitionKey", processDefinitionKey);
        }
        if (businessKey != null) {
            payload.put("businessKey", businessKey);
        }
        payload.put("time", System.currentTimeMillis());
        ssePublisher.sendToUser(initiatorId, SseConstants.EVENT_WORKFLOW_INSTANCE, payload);
    }

    /** 解析 Flowable 身份串为用户ID（assignee/startedBy 可能为空或非数字，解析失败则跳过推送） */
    static Long parseUserId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
