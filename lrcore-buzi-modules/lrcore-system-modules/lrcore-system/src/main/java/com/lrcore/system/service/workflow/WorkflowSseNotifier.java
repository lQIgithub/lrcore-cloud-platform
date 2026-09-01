package com.lrcore.system.service.workflow;

import org.flowable.task.api.Task;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 工作流事件 → SSE 实时推送通知器。
 *
 * <p>调用方：Flowable 引擎事件监听器（WorkflowSseConfig 注册）；
 * 业务代码在任务操作（签收/转办/终止）后也可直接调用，补充引擎事件覆盖不到的场景。
 *
 * <p>推送对象：
 * <ul>
 *   <li>任务事件 → 当前处理人（task.getAssignee()；为空则跳过推送并记 debug 日志）</li>
 *   <li>实例事件 → 流程发起人（startedBy；为空则跳过推送并记 debug 日志）</li>
 * </ul>
 *
 * @Version: 1.0
 */
public interface WorkflowSseNotifier {

    /** 新任务创建（含首派） */
    void notifyTaskCreated(Task task);

    /** 任务指派/转办（新处理人） */
    void notifyTaskAssigned(Task task);

    /** 任务完成 */
    void notifyTaskCompleted(Task task);

    /** 任务删除（终止/撤回） */
    void notifyTaskDeleted(Task task);

    /** 流程实例启动 */
    void notifyInstanceStarted(String processInstanceId, String processDefinitionKey, String businessKey, Long initiatorId);

    /** 流程实例正常完成 */
    void notifyInstanceCompleted(String processInstanceId, Long initiatorId);

    /** 流程实例挂起 */
    void notifyInstanceSuspended(String processInstanceId, Long initiatorId);

    /** 流程实例激活 */
    void notifyInstanceActivated(String processInstanceId, Long initiatorId);

    /** 流程实例被终止（PROCESS_CANCELLED） */
    void notifyInstanceCancelled(String processInstanceId, Long initiatorId);
}
