package com.echo.ragtry.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.echo.ragtry.config.DifyApiConfiguration;
import com.echo.ragtry.constant.CustomerServiceConstant;
import com.echo.ragtry.vo.ChatMessageRequest;
import com.echo.ragtry.vo.ChatMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dify API服务类
 */
@Service
@Slf4j
public class DifyService {

    @Resource
    private WebClient difyWebClient;

    @Autowired
    private DifyApiConfiguration.DifyApiProperties difyApiProperties;

    /**
     * 存储用户当前的对话会话：key=userId, value=conversationId
     */
    private static final Map<String, String> userConversations = new ConcurrentHashMap<>();

    /**
     * 发送消息到Dify
     */
    public ChatMessageResponse sendMessage(ChatMessageRequest request) {
        return sendMessage(request, "default-user");
    }

    /**
     * 发送消息到Dify
     */
    public ChatMessageResponse sendMessage(ChatMessageRequest request, String userId) {
        log.info("发送消息到Dify API: {}", request.getMessage());

        try {
            // 获取或创建用户的对话会话
            String conversationId = getUserConversationId(userId, request.getConversationId());

            // 构建发送给Dify的请求体
            Map<String, Object> difyRequest = buildDifyRequest(request, conversationId, userId);

            log.debug("Dify请求体: {}", JSON.toJSONString(difyRequest));

            // 调用Dify API获取响应
            String responseBody = difyWebClient
                    .post()
                    .uri(CustomerServiceConstant.ApiPath.CHAT_MESSAGES)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(difyRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Dify API响应: {}", responseBody);

            // 解析响应
            JSONObject difyResponse = JSON.parseObject(responseBody);

            // 更新会话ID
            String responseConversationId = difyResponse.getString("conversation_id");
            if (StringUtils.hasText(responseConversationId)) {
                userConversations.put(userId, responseConversationId);
            }

            // 构建响应
            ChatMessageResponse response = new ChatMessageResponse();
            response.setTaskId(difyResponse.getString("task_id"));
            response.setConversationId(responseConversationId != null ? responseConversationId : conversationId);
            response.setResponseMode(CustomerServiceConstant.ResponseMode.BLOCKING);
            response.setMessageId(difyResponse.getString("message_id"));
            response.setAnswer(difyResponse.getString("answer"));
            response.setSource(CustomerServiceConstant.SmartRouting.SERVICE_DIFY);

            log.info("Dify消息处理完成，回复长度: {}",
                    response.getAnswer() != null ? response.getAnswer().length() : 0);

            return response;

        } catch (Exception e) {
            log.error("Dify API调用失败: ", e);
            throw new RuntimeException("Dify API调用失败: " + e.getMessage());
        }
    }

    /**
     * 获取或创建用户的对话会话ID
     */
    private String getUserConversationId(String userId, String requestConversationId) {
        if (StringUtils.hasText(requestConversationId)) {
            // 如果请求中指定了会话ID，则使用并更新缓存
            userConversations.put(userId, requestConversationId);
            return requestConversationId;
        }

        // 从缓存中获取用户的当前会话
        return userConversations.get(userId);
    }

    /**
     * 构建发送给Dify的请求体
     */
    private Map<String, Object> buildDifyRequest(ChatMessageRequest request, String conversationId, String userId) {
        Map<String, Object> difyRequest = new HashMap<>();
        difyRequest.put("inputs", new HashMap<>());
        difyRequest.put("query", request.getMessage());
        difyRequest.put("response_mode", CustomerServiceConstant.ResponseMode.BLOCKING);
        difyRequest.put("user", StringUtils.hasText(request.getUser()) ? request.getUser() : userId);

        // 如果有会话ID，则继续之前的对话
        if (StringUtils.hasText(conversationId)) {
            difyRequest.put("conversation_id", conversationId);
        }

        return difyRequest;
    }

    /**
     * 清除用户会话
     */
    public void clearUserConversation(String userId) {
        userConversations.remove(userId);
        log.info("清除用户 {} 的Dify会话记录", userId);
    }

    /**
     * 获取用户当前会话ID
     */
    public String getUserCurrentConversationId(String userId) {
        return userConversations.get(userId);
    }

    /**
     * 检查Dify服务健康状态
     */
    public boolean checkHealth() {
        try {
            // 发送一个简单的测试消息
            ChatMessageRequest testRequest = new ChatMessageRequest();
            testRequest.setMessage("健康检查");

            ChatMessageResponse response = sendMessage(testRequest, "health-check-user");
            return response != null && StringUtils.hasText(response.getAnswer());
        } catch (Exception e) {
            log.warn("Dify健康检查失败: {}", e.getMessage());
            return false;
        }
    }
}

