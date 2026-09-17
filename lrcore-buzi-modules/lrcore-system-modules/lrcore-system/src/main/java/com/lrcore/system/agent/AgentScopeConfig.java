package com.lrcore.system.agent;

import io.agentscope.core.ReActAgent;
import io.agentscope.extensions.model.openai.OpenAIChatModel;
import io.agentscope.extensions.model.openai.formatter.OpenAIChatFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AgentScope 智能体配置类
 * 接入 GPUStack 模型服务（OpenAI 兼容接口）
 *
 * @author lrcore
 */
@Slf4j
@Configuration
public class AgentScopeConfig {

    @Value("${agentscope.openai.api-key:sk-5K5xKdKf3btpiBp2dDyoaXp9zDms2eNv7YhVbIfRAffojTsv}")
    private String apiKey;

    @Value("${agentscope.openai.base-url:http://10.10.10.103:3000/v1}")
    private String baseUrl;

    @Value("${agentscope.openai.model:DeepSeek-V4-Flash-0731-W8A8-INT8}")
    private String model;

    /**
     * 创建 DeepSeek 模型实例
     */
    @Bean
    public OpenAIChatModel deepSeekModel() {
        OpenAIChatModel modelObj = OpenAIChatModel.builder()
                .apiKey(apiKey)
                .modelName(model)
                .baseUrl(baseUrl)
                .formatter(new OpenAIChatFormatter())
                .nativeStructuredOutput(false)
                .nativeStructuredOutputWithTools(false)
                .build();
        log.info("DeepSeek 模型配置完成: model={}, baseUrl={}", model, baseUrl);
        return modelObj;
    }

    /**
     * 创建 Qwen 模型实例
     */
    @Bean
    public OpenAIChatModel qwenModel() {
        OpenAIChatModel modelObj = OpenAIChatModel.builder()
                .apiKey(apiKey)
                .modelName("Qwen3.8-27B")
                .baseUrl(baseUrl)
                .formatter(new OpenAIChatFormatter())
                .nativeStructuredOutput(false)
                .nativeStructuredOutputWithTools(false)
                .build();
        log.info("Qwen 模型配置完成: model=Qwen3.8-27B, baseUrl={}", baseUrl);
        return modelObj;
    }

    /**
     * 创建系统助手智能体
     */
    @Bean("defaultReActAgent")
    public ReActAgent defaultReActAgent(OpenAIChatModel deepSeekModel) {
        ReActAgent agent = ReActAgent.builder()
                .name("系统助手")
                .sysPrompt("你是一个专业的系统助手，名叫小智。你的职责是：\n" +
                        "1. 友好、专业地回答用户的问题\n" +
                        "2. 提供准确、有用的信息和建议\n" +
                        "3. 在不确定时诚实地告知用户\n" +
                        "4. 使用中文与用户交流")
                .model(deepSeekModel)
                .maxIters(5)
                .build();

        log.info("系统助手智能体创建完成: name={}", agent.getName());
        return agent;
    }

    /**
     * 创建 DeepSeek 代码助手智能体
     */
    @Bean
    public ReActAgent deepSeekAgent(OpenAIChatModel deepSeekModel) {
        ReActAgent agent = ReActAgent.builder()
                .name("DeepSeek助手")
                .sysPrompt("你是 DeepSeek 助手，擅长代码生成和技术问题解答。")
                .model(deepSeekModel)
                .maxIters(5)
                .build();

        log.info("DeepSeek 智能体创建完成: name={}", agent.getName());
        return agent;
    }

    /**
     * 创建 Qwen 知识问答智能体
     */
    @Bean
    public ReActAgent qwenAgent(OpenAIChatModel qwenModel) {
        ReActAgent agent = ReActAgent.builder()
                .name("Qwen助手")
                .sysPrompt("你是 Qwen 助手，擅长自然语言处理和知识问答。")
                .model(qwenModel)
                .maxIters(5)
                .build();

        log.info("Qwen 智能体创建完成: name={}", agent.getName());
        return agent;
    }
}