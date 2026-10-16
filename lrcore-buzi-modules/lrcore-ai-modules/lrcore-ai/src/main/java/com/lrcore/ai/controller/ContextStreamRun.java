package com.lrcore.ai.controller;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEventType;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.compaction.CompactionConfig;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
@AllArgsConstructor
public class ContextStreamRun implements ApplicationRunner {

    @Qualifier("qwenModel")
    private OpenAIChatModel qwenMode;

    @Override
    public void run(ApplicationArguments args) {
        try (HarnessAgent agent = HarnessAgent.builder()
                .name("quickStart")
                .model(qwenMode)
                .workspace(Paths.get(".agentscope/workspace"))
                .compaction(CompactionConfig.builder()
                        .triggerMessages(30)
                        .keepMessages(10)
                        .build())
                .build()
        ) {
            // 上下文设置
            RuntimeContext ctx = RuntimeContext.builder().userId("dashuaige").sessionId("session-1").build();
            // 添加上下文， 使用流式输出内容， 后期配置SSE实现推流
            agent.streamEvents(Msg.builder().textContent("你知道我喜欢什么吗").build(), ctx)
                    .doOnNext(event -> {
//                        if(event instanceof TextBlockDeltaEvent deltaEvent){
//                            System.out.println(deltaEvent.getDelta());
//                            switch (event.getType()){
//                                    case AgentEventType.TEXT_BLOCK_DELTA -> System.out.print(deltaEvent.getDelta());
//                                    default -> {}
//                            }
//                        } else if (event instanceof ToolCallStartEvent toolStartEvent) {
//                            switch (event.getType()){
//                                case AgentEventType.TOOL_CALL_START -> System.out.println("\n[tool] " +  toolStartEvent.getToolCallName());
//                                default -> {}
//                            }
//                        }
                        if (event.getType() == AgentEventType.TEXT_BLOCK_DELTA) {
                            // 模型返回的流式文本片段 --- 追加到界面货标准输出
                            String delta = ((TextBlockDeltaEvent) event).getDelta();
                            System.out.println(delta);
                        }else if (event.getType() == AgentEventType.TOOL_CALL_START){
                            // 智能体即将调用工具。-- 展示调用信息
                            System.out.println("\n[tool] " +  ((ToolCallStartEvent)event).getToolCallName());
                        }
                        // 其他事件： 思考块、工具返回结果、回复结束等

                    })
                    // 注阻塞才行
                    .blockLast();
        }
    }
}