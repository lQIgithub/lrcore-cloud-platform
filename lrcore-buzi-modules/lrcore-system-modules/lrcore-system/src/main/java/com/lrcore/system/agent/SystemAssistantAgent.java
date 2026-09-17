package com.lrcore.system.agent;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.message.Msg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统助手智能体实现
 * <p>
 * 基于 AgentScope Java 2.0 ReActAgent 构建的智能体封装
 * </p>
 *
 * @author lrcore
 */
@Slf4j
@Component
public class SystemAssistantAgent {

    private final ReActAgent agent;

    public SystemAssistantAgent(@Qualifier("defaultReActAgent") ReActAgent systemAssistantAgent) {
        this.agent = systemAssistantAgent;
        log.info("SystemAssistantAgent 初始化完成");
    }

    /**
     * 处理用户消息并返回智能体回复
     *
     * @param userMessage 用户消息
     * @return 智能体回复
     */
    public String chat(String userMessage) {
        log.info("收到用户消息: {}", userMessage);

        try {
            Msg userMsg = Msg.builder()
                    .name("user")
                    .textContent(userMessage)
                    .build();

            Msg response = agent.call(userMsg).block();

            String reply = response != null ? response.getTextContent() : "无回复";
            log.info("智能体回复: {}", reply);
            return reply;

        } catch (Exception e) {
            log.error("智能体调用失败", e);
            return "抱歉，处理您的请求时出现了错误: " + e.getMessage();
        }
    }

    /**
     * 带上下文的对话（将历史消息合并为单条消息发送）
     *
     * @param messages 消息历史，每条包含 role 和 content
     * @return 智能体回复
     */
    public String chatWithContext(List<Map<String, String>> messages) {
        log.info("收到带上下文的对话请求，消息数量: {}", messages.size());

        try {
            // 将上下文消息格式化为单条提示
            String context = messages.stream()
                    .map(msg -> {
                        String role = msg.getOrDefault("role", "user");
                        String content = msg.getOrDefault("content", "");
                        return switch (role.toLowerCase()) {
                            case "user" -> "用户: " + content;
                            case "assistant" -> "助手: " + content;
                            case "system" -> "系统: " + content;
                            default -> role + ": " + content;
                        };
                    })
                    .collect(Collectors.joining("\n"));

            // 取最后一条用户消息作为实际输入
            String lastUserMessage = messages.stream()
                    .filter(msg -> "user".equalsIgnoreCase(msg.get("role")))
                    .reduce((first, second) -> second)
                    .map(msg -> msg.get("content"))
                    .orElse(context);

            // 如果有多轮上下文，附加到提示中
            String finalPrompt;
            if (messages.size() > 1) {
                finalPrompt = "以下是对话历史：\n" + context + "\n\n请基于以上对话历史回答最后一个问题。";
            } else {
                finalPrompt = lastUserMessage;
            }

            Msg userMsg = Msg.builder()
                    .name("user")
                    .textContent(finalPrompt)
                    .build();

            Msg response = agent.call(userMsg).block();

            String reply = response != null ? response.getTextContent() : "无回复";
            log.info("智能体回复: {}", reply);
            return reply;

        } catch (Exception e) {
            log.error("智能体调用失败", e);
            return "抱歉，处理您的请求时出现了错误: " + e.getMessage();
        }
    }

    /**
     * 获取智能体信息
     *
     * @return 智能体信息
     */
    public Map<String, Object> getAgentInfo() {
        return Map.of(
                "name", agent.getName(),
                "description", "系统助手智能体",
                "status", "active"
        );
    }
}