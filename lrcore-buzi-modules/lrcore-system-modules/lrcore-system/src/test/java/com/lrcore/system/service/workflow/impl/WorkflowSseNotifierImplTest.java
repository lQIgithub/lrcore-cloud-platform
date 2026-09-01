package com.lrcore.system.service.workflow.impl;

import com.lrcore.system.service.sse.SsePublisher;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * WorkflowSseNotifierImpl 单元测试：推送对象选择、载荷组装与用户ID解析。
 */
class WorkflowSseNotifierImplTest {

    private SsePublisher ssePublisher;
    private WorkflowSseNotifierImpl notifier;
    private Task task;

    @BeforeEach
    void setUp() {
        ssePublisher = mock(SsePublisher.class);
        notifier = new WorkflowSseNotifierImpl(ssePublisher);
        task = mock(Task.class);
    }

    private static void stubTaskWithAssignee(Task task, String assignee) {
        when(task.getId()).thenReturn("101");
        when(task.getAssignee()).thenReturn(assignee);
        when(task.getProcessInstanceId()).thenReturn("55");
        when(task.getName()).thenReturn("审批");
        when(task.getProcessDefinitionId()).thenReturn("leave:1:1");
        when(task.getTaskDefinitionKey()).thenReturn("activity1");
    }

    @Test
    void notifyTaskCreated_publishesToAssigneeWithFullPayload() {
        stubTaskWithAssignee(task, "8");

        notifier.notifyTaskCreated(task);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(ssePublisher).sendToUser(eq(8L), eq("workflow-task"), payload.capture());
        assertThat(payload.getValue())
                .containsEntry("type", "created")
                .containsEntry("taskId", "101")
                .containsEntry("processInstanceId", "55")
                .containsEntry("taskName", "审批")
                .containsEntry("processDefinitionId", "leave:1:1")
                .containsEntry("taskDefinitionKey", "activity1")
                .containsEntry("assigneeId", "8");
        assertThat(payload.getValue().get("time")).isInstanceOf(Long.class);
    }

    @Test
    void taskEventTypes_mapToWireValues() {
        stubTaskWithAssignee(task, "8");

        notifier.notifyTaskAssigned(task);
        notifier.notifyTaskCompleted(task);
        notifier.notifyTaskDeleted(task);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(ssePublisher, times(3)).sendToUser(eq(8L), eq("workflow-task"), payload.capture());
        assertThat(payload.getAllValues()).extracting(map -> map.get("type"))
                .containsExactly("assigned", "completed", "deleted");
    }

    @Test
    void notifyTaskCreated_withoutAssignee_skipsPublish() {
        when(task.getId()).thenReturn("101");
        when(task.getAssignee()).thenReturn(null);

        notifier.notifyTaskCreated(task);

        verifyNoInteractions(ssePublisher);
    }

    @Test
    void notifyTaskCreated_withNonNumericAssignee_skipsPublish() {
        when(task.getId()).thenReturn("101");
        when(task.getAssignee()).thenReturn("candidate-group");

        notifier.notifyTaskCreated(task);

        verifyNoInteractions(ssePublisher);
    }

    @Test
    void notifyTaskWithNullTask_isNoOp() {
        notifier.notifyTaskCompleted(null);
        notifier.notifyTaskCreated(null);

        verifyNoInteractions(ssePublisher);
    }

    @Test
    void notifyInstanceStarted_publishesToInitiatorWithKeyAndBusinessKey() {
        notifier.notifyInstanceStarted("55", "leave", "biz-1", 9L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(ssePublisher).sendToUser(eq(9L), eq("workflow-instance"), payload.capture());
        assertThat(payload.getValue())
                .containsEntry("type", "started")
                .containsEntry("processInstanceId", "55")
                .containsEntry("processDefinitionKey", "leave")
                .containsEntry("businessKey", "biz-1");
    }

    @Test
    void instanceEventTypes_mapToWireValues() {
        notifier.notifyInstanceCompleted("55", 9L);
        notifier.notifyInstanceSuspended("55", 9L);
        notifier.notifyInstanceActivated("55", 9L);
        notifier.notifyInstanceCancelled("55", 9L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> payload = ArgumentCaptor.forClass(Map.class);
        verify(ssePublisher, times(4)).sendToUser(eq(9L), eq("workflow-instance"), payload.capture());
        assertThat(payload.getAllValues()).extracting(map -> map.get("type"))
                .containsExactly("completed", "suspended", "activated", "cancelled");
    }

    @Test
    void notifyInstanceWithoutInitiatorOrInstanceId_skipsPublish() {
        notifier.notifyInstanceCompleted("55", null);
        notifier.notifyInstanceStarted(null, "leave", "biz-1", 9L);

        verifyNoInteractions(ssePublisher);
    }

    @Test
    void parseUserId_nullBlankAndNumeric() {
        assertThat(WorkflowSseNotifierImpl.parseUserId(null)).isNull();
        assertThat(WorkflowSseNotifierImpl.parseUserId("  ")).isNull();
        assertThat(WorkflowSseNotifierImpl.parseUserId("42")).isEqualTo(42L);
        assertThat(WorkflowSseNotifierImpl.parseUserId(" 42 ")).isEqualTo(42L);
        assertThat(WorkflowSseNotifierImpl.parseUserId("abc")).isNull();
    }
}
