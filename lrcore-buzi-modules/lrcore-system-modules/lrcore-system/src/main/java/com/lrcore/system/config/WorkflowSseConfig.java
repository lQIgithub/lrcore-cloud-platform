package com.lrcore.system.config;

import com.lrcore.system.service.workflow.WorkflowSseNotifier;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.AbstractFlowableEventListener;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventType;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.task.api.Task;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>类模块说明</p>
 *
 * @Describe: 工作流事件 → SSE 实时推送接线（Flowable 8 引擎扩展点）。
 *
 * <p>机制：{@link EngineConfigurationConfigurer} Bean 由 flowable-spring-boot-autoconfigure
 * 自动收集并在 buildEngine 前应用到引擎配置（与 lrcore-common-flowable 的
 * FlowableConfig#engineConfigurationConfigurer 同一机制）。此处注册引擎级事件监听器：
 * 任务/实例事件触发时经 {@link WorkflowSseNotifier} 推送给对应用户。
 *
 * <p>事件映射（Flowable 8.0.0 无 TASK_DELETED / PROCESS_SUSPENDED / PROCESS_ACTIVATED
 * 专用常量，改用通用 ENTITY_* 事件并按实体类型过滤）：
 * <ul>
 *   <li>TASK_CREATED / TASK_ASSIGNED / TASK_COMPLETED → workflow-task（type=created/assigned/completed）</li>
 *   <li>ENTITY_DELETED（实体=Task）→ workflow-task（type=deleted）</li>
 *   <li>PROCESS_STARTED / PROCESS_COMPLETED / PROCESS_CANCELLED → workflow-instance（type=started/completed/cancelled）</li>
 *   <li>ENTITY_SUSPENDED / ENTITY_ACTIVATED（实体=ExecutionEntity）→ workflow-instance（type=suspended/activated）</li>
 * </ul>
 *
 * <p>监听器 onTransaction 为 null（事件立即派发）；推送失败仅记 warn，绝不影响工作流主流程。
 *
 * @Version: 1.0
 */
@Slf4j
@Configuration
public class WorkflowSseConfig {

    private final WorkflowSseNotifier workflowSseNotifier;

    public WorkflowSseConfig(WorkflowSseNotifier workflowSseNotifier) {
        this.workflowSseNotifier = workflowSseNotifier;
    }

    /** 向 Flowable 引擎注册 SSE 事件监听器 */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> workflowSseEngineConfigurer() {
        return config -> {
            // 注意：引擎配置的 eventListeners 字段无惰性初始化，本配置器运行时机（buildEngine 前）
            // 早于 initEventListeners()，getEventListeners() 可能为 null——此时需自建列表并回写，
            // 引擎 init 阶段的 initEventListeners() 会将列表内监听器统一注册到事件分发器。
            List<FlowableEventListener> eventListeners = config.getEventListeners();
            if (eventListeners == null) {
                eventListeners = new ArrayList<>();
                config.setEventListeners(eventListeners);
            }
            eventListeners.add(new WorkflowSseEngineEventListener(workflowSseNotifier));
            log.info("工作流 SSE：已注册 Flowable 引擎事件监听器（workflow-task / workflow-instance）");
        };
    }

    /** Flowable 引擎事件 → WorkflowSseNotifier（包级可见，便于单元测试） */
    static final class WorkflowSseEngineEventListener extends AbstractFlowableEventListener {

        private final WorkflowSseNotifier notifier;

        WorkflowSseEngineEventListener(WorkflowSseNotifier notifier) {
            this.notifier = notifier;
        }

        @Override
        public void onEvent(FlowableEvent event) {
            try {
                dispatch(event);
            } catch (Exception e) {
                // 推送失败不得影响工作流主流程
                log.warn("Flowable 事件 SSE 推送失败（不影响主流程）", e);
            }
        }

        @Override
        public boolean isFailOnException() {
            return false;
        }

        @Override
        public Set<FlowableEngineEventType> getTypes() {
            return Set.of(
                    FlowableEngineEventType.TASK_CREATED,
                    FlowableEngineEventType.TASK_ASSIGNED,
                    FlowableEngineEventType.TASK_COMPLETED,
                    FlowableEngineEventType.PROCESS_STARTED,
                    FlowableEngineEventType.PROCESS_COMPLETED,
                    FlowableEngineEventType.PROCESS_CANCELLED,
                    FlowableEngineEventType.ENTITY_SUSPENDED,
                    FlowableEngineEventType.ENTITY_ACTIVATED,
                    FlowableEngineEventType.ENTITY_DELETED);
        }

        private void dispatch(FlowableEvent event) {
            if (!(event instanceof FlowableEngineEntityEvent entityEvent)) {
                return;
            }
            if (!(event.getType() instanceof FlowableEngineEventType type)) {
                return;
            }
            Object entity = entityEvent.getEntity();
            switch (type) {
                case TASK_CREATED -> notifier.notifyTaskCreated(asTask(entity));
                case TASK_ASSIGNED -> notifier.notifyTaskAssigned(asTask(entity));
                case TASK_COMPLETED -> notifier.notifyTaskCompleted(asTask(entity));
                case ENTITY_DELETED -> {
                    if (entity instanceof Task task) {
                        notifier.notifyTaskDeleted(task);
                    }
                }
                case PROCESS_STARTED -> notifier.notifyInstanceStarted(
                        entityEvent.getProcessInstanceId(),
                        definitionKeyOf(entity),
                        businessKeyOf(entity),
                        startUserIdOf(entity));
                case PROCESS_COMPLETED -> notifier.notifyInstanceCompleted(
                        entityEvent.getProcessInstanceId(), startUserIdOf(entity));
                case PROCESS_CANCELLED -> notifier.notifyInstanceCancelled(
                        entityEvent.getProcessInstanceId(), startUserIdOf(entity));
                case ENTITY_SUSPENDED -> {
                    if (entity instanceof ExecutionEntity) {
                        notifier.notifyInstanceSuspended(entityEvent.getProcessInstanceId(), startUserIdOf(entity));
                    }
                }
                case ENTITY_ACTIVATED -> {
                    if (entity instanceof ExecutionEntity) {
                        notifier.notifyInstanceActivated(entityEvent.getProcessInstanceId(), startUserIdOf(entity));
                    }
                }
                default -> {
                    // 其余事件类型不推送
                }
            }
        }

        private static Task asTask(Object entity) {
            if (!(entity instanceof Task task)) {
                throw new IllegalStateException("任务事件实体类型异常："
                        + (entity == null ? "null" : entity.getClass().getName()));
            }
            return task;
        }

        private static String definitionKeyOf(Object entity) {
            return entity instanceof ExecutionEntity executionEntity
                    ? executionEntity.getProcessDefinitionKey()
                    : null;
        }

        private static String businessKeyOf(Object entity) {
            return entity instanceof ExecutionEntity executionEntity
                    ? executionEntity.getBusinessKey()
                    : null;
        }

        private static Long startUserIdOf(Object entity) {
            if (!(entity instanceof ExecutionEntity executionEntity)) {
                return null;
            }
            String startUserId = executionEntity.getStartUserId();
            if (startUserId == null || startUserId.isBlank()) {
                return null;
            }
            try {
                return Long.parseLong(startUserId.trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }
}
