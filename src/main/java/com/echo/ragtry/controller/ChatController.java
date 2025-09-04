package com.echo.ragtry.controller;

import com.echo.ragtry.service.SmartRoutingService;
import com.echo.ragtry.vo.ChatMessageRequest;
import com.echo.ragtry.vo.ChatMessageResponse;
import com.echo.ragtry.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 智能聊天控制器
 * 提供统一的聊天接口，支持智能路由
 */
@RestController
@RequestMapping("/api/chat")
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private SmartRoutingService smartRoutingService;

    /**
     * 智能聊天接口
     * 根据配置策略自动选择Dify或RAG服务
     */
    @PostMapping("/send")
    public Result<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        log.info("收到聊天请求: {}", request.getMessage());

        try {
            ChatMessageResponse response = smartRoutingService.routeMessage(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("处理聊天请求失败", e);
            return Result.error("处理聊天请求失败: " + e.getMessage());
        }
    }

    /**
     * 带用户ID的智能聊天接口
     */
    @PostMapping("/send/{userId}")
    public Result<ChatMessageResponse> sendMessageWithUserId(
            @PathVariable String userId,
            @Valid @RequestBody ChatMessageRequest request) {
        log.info("收到用户 {} 的聊天请求: {}", userId, request.getMessage());

        try {
            ChatMessageResponse response = smartRoutingService.routeMessage(request, userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("处理用户 {} 的聊天请求失败", userId, e);
            return Result.error("处理聊天请求失败: " + e.getMessage());
        }
    }

    /**
     * 获取推荐的路由策略
     */
    @GetMapping("/strategy/recommend")
    public Result<String> getRecommendedStrategy() {
        try {
            String strategy = smartRoutingService.getRecommendedStrategy();
            return Result.success(strategy);
        } catch (Exception e) {
            log.error("获取推荐策略失败", e);
            return Result.error("获取推荐策略失败: " + e.getMessage());
        }
    }

    /**
     * 检查服务健康状态
     */
    @GetMapping("/health")
    public Result<String> checkHealth() {
        try {
            boolean isHealthy = smartRoutingService.checkServicesHealth();
            if (isHealthy) {
                return Result.success("服务运行正常");
            } else {
                return Result.error(503, "服务不可用");
            }
        } catch (Exception e) {
            log.error("健康检查失败", e);
            return Result.error("健康检查失败: " + e.getMessage());
        }
    }
}

