package com.echo.ragtry.controller;

import com.echo.ragtry.service.DifyService;
import com.echo.ragtry.vo.ChatMessageRequest;
import com.echo.ragtry.vo.ChatMessageResponse;
import com.echo.ragtry.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Dify API控制器
 * 提供直接调用Dify服务的接口
 */
@RestController
@RequestMapping("/api/dify")
@Slf4j
@CrossOrigin(origins = "*")
public class DifyController {

    @Autowired
    private DifyService difyService;

    /**
     * 发送消息到Dify
     */
    @PostMapping("/chat")
    public Result<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        log.info("直接调用Dify API: {}", request.getMessage());

        try {
            ChatMessageResponse response = difyService.sendMessage(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("Dify API调用失败", e);
            return Result.error("Dify API调用失败: " + e.getMessage());
        }
    }

    /**
     * 带用户ID的Dify聊天接口
     */
    @PostMapping("/chat/{userId}")
    public Result<ChatMessageResponse> sendMessageWithUserId(
            @PathVariable String userId,
            @Valid @RequestBody ChatMessageRequest request) {
        log.info("用户 {} 直接调用Dify API: {}", userId, request.getMessage());

        try {
            ChatMessageResponse response = difyService.sendMessage(request, userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("用户 {} 的Dify API调用失败", userId, e);
            return Result.error("Dify API调用失败: " + e.getMessage());
        }
    }

    /**
     * 清除用户会话
     */
    @DeleteMapping("/conversation/{userId}")
    public Result<String> clearUserConversation(@PathVariable String userId) {
        log.info("清除用户 {} 的Dify会话", userId);

        try {
            difyService.clearUserConversation(userId);
            return Result.success("会话已清除");
        } catch (Exception e) {
            log.error("清除用户会话失败", e);
            return Result.error("清除会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户当前会话ID
     */
    @GetMapping("/conversation/{userId}")
    public Result<String> getUserConversationId(@PathVariable String userId) {
        try {
            String conversationId = difyService.getUserCurrentConversationId(userId);
            return Result.success(conversationId);
        } catch (Exception e) {
            log.error("获取用户会话ID失败", e);
            return Result.error("获取会话ID失败: " + e.getMessage());
        }
    }

    /**
     * Dify服务健康检查
     */
    @GetMapping("/health")
    public Result<String> checkHealth() {
        try {
            boolean isHealthy = difyService.checkHealth();
            if (isHealthy) {
                return Result.success("Dify服务运行正常");
            } else {
                return Result.error(503, "Dify服务不可用");
            }
        } catch (Exception e) {
            log.error("Dify健康检查失败", e);
            return Result.error("Dify健康检查失败: " + e.getMessage());
        }
    }
}

