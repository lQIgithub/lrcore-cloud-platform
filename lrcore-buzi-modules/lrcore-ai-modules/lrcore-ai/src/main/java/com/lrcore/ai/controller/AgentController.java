package com.lrcore.ai.controller;

import com.lrcore.common.ai.config.SystemAssistantAgent;
import com.lrcore.common.core.web.domain.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>类模块说明</p>

 * @Describe: 智能体控制器
 * <p>
 * 提供智能体对话的 REST API 接口
 * </p>
 *
 * @ClassName: AgentController
 * @Author: Qi Liu
 * @Date: 2026/9/17 23:13
 * @Version: 1.0
 */
@Slf4j
@Tag(name = "智能体管理", description = "智能体对话相关接口")
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {

    private final SystemAssistantAgent systemAssistantAgent;

    /**
     * 简单对话请求
     */
    @Data
    public static class ChatRequest {
        @Parameter(description = "用户消息", required = true)
        private String message;
    }

    /**
     * 带上下文的对话请求
     */
    @Data
    public static class ContextChatRequest {
        @Parameter(description = "消息列表", required = true)
        private List<Map<String, String>> messages;
    }

    /**
     * 对话响应
     */
    @Data
    public static class ChatResponse {
        private String reply;
        private String agentName;
        private long timestamp;

        public ChatResponse(String reply, String agentName) {
            this.reply = reply;
            this.agentName = agentName;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * 简单对话接口
     *
     * @param request 对话请求
     * @return 智能体回复
     */
    @Operation(summary = "简单对话", description = "发送消息给智能体并获取回复")
    @PostMapping("/chat")
    public ApiResult<ChatResponse> chat(@RequestBody ChatRequest request) {
        log.info("收到对话请求: {}", request.getMessage());
        
        String reply = systemAssistantAgent.chat(request.getMessage());
        ChatResponse response = new ChatResponse(reply, "系统助手");
        
        return ApiResult.success(response);
    }

    /**
     * 带上下文的对话接口
     *
     * @param request 上下文对话请求
     * @return 智能体回复
     */
    @Operation(summary = "带上下文对话", description = "发送带历史消息的对话请求")
    @PostMapping("/chat/context")
    public ApiResult<ChatResponse> chatWithContext(@RequestBody ContextChatRequest request) {
        log.info("收到带上下文的对话请求，消息数量: {}", request.getMessages().size());
        
        String reply = systemAssistantAgent.chatWithContext(request.getMessages());
        ChatResponse response = new ChatResponse(reply, "系统助手");
        
        return ApiResult.success(response);
    }

    /**
     * 获取智能体信息
     *
     * @return 智能体信息
     */
    @Operation(summary = "获取智能体信息", description = "获取当前智能体的基本信息")
    @GetMapping("/info")
    public ApiResult<Map<String, Object>> getAgentInfo() {
        Map<String, Object> info = systemAssistantAgent.getAgentInfo();
        return ApiResult.success(info);
    }

    /**
     * 健康检查
     *
     * @return 健康状态
     */
    @Operation(summary = "健康检查", description = "检查智能体服务是否正常")
    @GetMapping("/health")
    public ApiResult<String> health() {
        return ApiResult.success("智能体服务运行正常");
    }
}
