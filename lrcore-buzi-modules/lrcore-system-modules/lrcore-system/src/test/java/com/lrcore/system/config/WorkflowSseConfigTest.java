package com.lrcore.system.config;

import com.lrcore.system.service.workflow.WorkflowSseNotifier;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.task.api.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * WorkflowSseConfig 单元测试：配置器注册 + 引擎事件 → 通知器分发映射。
 */
class WorkflowSseConfigTest {

    private WorkflowSseNotifier notifier;
    private WorkflowSseConfig config;
    private WorkflowSseConfig.WorkflowSseEngineEventListener listener;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        notifier = mock(WorkflowSseNotifier.class);
        config = new WorkflowSseConfig(notifier);

        // 真实引擎行为：配置器运行早于 initEventListeners()，eventListeners 字段未初始化 →
        // getEventListeners() 返回 null（AbstractEngineConfiguration 无惰性初始化）
        listener = applyConfigurerToFreshEngine();
    }

    /** 将配置器应用到【eventListeners 为 null】的模拟引擎配置，取回注册的监听器 */
    @SuppressWarnings("unchecked")
    private WorkflowSseConfig.WorkflowSseEngineEventListener applyConfigurerToFreshEngine() {
        EngineConfigurationConfigurer<SpringProcessEngineConfiguration> configurer =
                config.workflowSseEngineConfigurer();
        SpringProcessEngineConfiguration engineConfig = mock(SpringProcessEngineConfiguration.class);
        when(engineConfig.getEventListeners()).thenReturn(null);
        configurer.configure(engineConfig);

        org.mockito.ArgumentCaptor<List<FlowableEventListener>> captor =
                org.mockito.ArgumentCaptor.forClass(List.class);
        verify(engineConfig).setEventListeners(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        return (WorkflowSseConfig.WorkflowSseEngineEventListener) captor.getValue().get(0);
    }

    @Test
    @SuppressWarnings("unchecked")
    void configurer_appendsToPreExistingListenerList_withoutRecreating() {
        EngineConfigurationConfigurer<SpringProcessEngineConfiguration> configurer =
                config.workflowSseEngineConfigurer();
        SpringProcessEngineConfiguration engineConfig = mock(SpringProcessEngineConfiguration.class);
        List<FlowableEventListener> preExisting = new ArrayList<>();
        when(engineConfig.getEventListeners()).thenReturn(preExisting);

        configurer.configure(engineConfig);

        assertThat(preExisting).hasSize(1);
        org.mockito.Mockito.verify(engineConfig, org.mockito.Mockito.never())
                .setEventListeners(org.mockito.ArgumentMatchers.anyList());
    }

    private static FlowableEngineEntityEvent engineEvent(FlowableEngineEventType type, Object entity) {
        FlowableEngineEntityEvent event = mock(FlowableEngineEntityEvent.class);
        when(event.getType()).thenReturn(type);
        when(event.getEntity()).thenReturn(entity);
        return event;
    }

    @Test
    void listener_subscribesExpectedEventTypes() {
        Set<FlowableEngineEventType> types = listener.getTypes();

        assertThat(types)
                .contains(
                        FlowableEngineEventType.TASK_CREATED,
                        FlowableEngineEventType.TASK_ASSIGNED,
                        FlowableEngineEventType.TASK_COMPLETED,
                        FlowableEngineEventType.PROCESS_STARTED,
                        FlowableEngineEventType.PROCESS_COMPLETED,
                        FlowableEngineEventType.PROCESS_CANCELLED,
                        FlowableEngineEventType.ENTITY_SUSPENDED,
                        FlowableEngineEventType.ENTITY_ACTIVATED,
                        FlowableEngineEventType.ENTITY_DELETED);
        assertThat(listener.isFailOnException()).isFalse();
    }

    @Test
    void taskCreatedEvent_dispatchesToNotifier() {
        Task task = mock(Task.class);
        listener.onEvent(engineEvent(FlowableEngineEventType.TASK_CREATED, task));

        verify(notifier).notifyTaskCreated(task);
    }

    @Test
    void taskAssignedAndCompletedEvents_dispatchToNotifier() {
        Task task = mock(Task.class);
        listener.onEvent(engineEvent(FlowableEngineEventType.TASK_ASSIGNED, task));
        listener.onEvent(engineEvent(FlowableEngineEventType.TASK_COMPLETED, task));

        verify(notifier).notifyTaskAssigned(task);
        verify(notifier).notifyTaskCompleted(task);
    }

    @Test
    void entityDeletedEvent_withTaskEntity_dispatchesToTaskDeleted() {
        Task task = mock(Task.class);
        listener.onEvent(engineEvent(FlowableEngineEventType.ENTITY_DELETED, task));

        verify(notifier).notifyTaskDeleted(task);
    }

    @Test
    void entityDeletedEvent_withNonTaskEntity_isIgnored() {
        ExecutionEntity execution = mock(ExecutionEntity.class);
        listener.onEvent(engineEvent(FlowableEngineEventType.ENTITY_DELETED, execution));

        verifyNoInteractions(notifier);
    }

    @Test
    void processStartedEvent_extractsInitiatorKeyAndBusinessKey() {
        ExecutionEntity entity = mock(ExecutionEntity.class);
        when(entity.getStartUserId()).thenReturn("9");
        when(entity.getProcessDefinitionKey()).thenReturn("leave");
        when(entity.getBusinessKey()).thenReturn("biz-1");
        FlowableEngineEntityEvent event = engineEvent(FlowableEngineEventType.PROCESS_STARTED, entity);
        when(event.getProcessInstanceId()).thenReturn("55");

        listener.onEvent(event);

        verify(notifier).notifyInstanceStarted("55", "leave", "biz-1", 9L);
    }

    @Test
    void processStartedEvent_withoutStartUserId_dispatchesNullInitiator() {
        ExecutionEntity entity = mock(ExecutionEntity.class);
        when(entity.getStartUserId()).thenReturn(null);
        FlowableEngineEntityEvent event = engineEvent(FlowableEngineEventType.PROCESS_STARTED, entity);
        when(event.getProcessInstanceId()).thenReturn("55");

        listener.onEvent(event);

        verify(notifier).notifyInstanceStarted("55", null, null, null);
    }

    @Test
    void processCompletedAndCancelledEvents_dispatchToNotifier() {
        ExecutionEntity entity = mock(ExecutionEntity.class);
        when(entity.getStartUserId()).thenReturn("9");

        FlowableEngineEntityEvent completed = engineEvent(FlowableEngineEventType.PROCESS_COMPLETED, entity);
        when(completed.getProcessInstanceId()).thenReturn("55");
        listener.onEvent(completed);

        FlowableEngineEntityEvent cancelled = engineEvent(FlowableEngineEventType.PROCESS_CANCELLED, entity);
        when(cancelled.getProcessInstanceId()).thenReturn("55");
        listener.onEvent(cancelled);

        verify(notifier).notifyInstanceCompleted("55", 9L);
        verify(notifier).notifyInstanceCancelled("55", 9L);
    }

    @Test
    void entitySuspendedAndActivatedEvents_withProcessInstance_dispatchToNotifier() {
        ExecutionEntity entity = mock(ExecutionEntity.class);
        when(entity.getStartUserId()).thenReturn("9");

        FlowableEngineEntityEvent suspended = engineEvent(FlowableEngineEventType.ENTITY_SUSPENDED, entity);
        when(suspended.getProcessInstanceId()).thenReturn("55");
        listener.onEvent(suspended);

        FlowableEngineEntityEvent activated = engineEvent(FlowableEngineEventType.ENTITY_ACTIVATED, entity);
        when(activated.getProcessInstanceId()).thenReturn("55");
        listener.onEvent(activated);

        verify(notifier).notifyInstanceSuspended("55", 9L);
        verify(notifier).notifyInstanceActivated("55", 9L);
    }

    @Test
    void entitySuspendedEvent_withTaskEntity_isIgnored() {
        Task task = mock(Task.class);
        listener.onEvent(engineEvent(FlowableEngineEventType.ENTITY_SUSPENDED, task));

        verifyNoInteractions(notifier);
    }

    @Test
    void nonEngineEntityEvent_isIgnored() {
        org.flowable.common.engine.api.delegate.event.FlowableEvent plain =
                mock(org.flowable.common.engine.api.delegate.event.FlowableEvent.class);
        when(plain.getType()).thenReturn(FlowableEngineEventType.TASK_CREATED);

        listener.onEvent(plain);

        verifyNoInteractions(notifier);
    }

    @Test
    void notifierFailure_isSwallowedByListener() {
        Task task = mock(Task.class);
        org.mockito.Mockito.doThrow(new IllegalStateException("push failed"))
                .when(notifier).notifyTaskCreated(task);

        assertThatCode(() -> listener.onEvent(engineEvent(FlowableEngineEventType.TASK_CREATED, task)))
                .doesNotThrowAnyException();
    }
}
