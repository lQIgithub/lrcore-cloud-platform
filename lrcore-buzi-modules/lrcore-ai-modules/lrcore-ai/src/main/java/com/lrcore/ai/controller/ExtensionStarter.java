package com.lrcore.ai.controller;

import io.agentscope.core.agent.RuntimeContext;
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
public class ExtensionStarter implements ApplicationRunner {

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
            // 添加上下文
//            Msg block = agent.call(Msg.builder().textContent("你知道我喜欢什么吗").build(), ctx).block();
//            System.out.println("大模型返回消息：" + block.getTextContent());
        }
    }
}