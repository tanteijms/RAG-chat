package com.echo.ragtry.service;

import com.echo.ragtry.config.SmartRoutingConfiguration;
import com.echo.ragtry.constant.CustomerServiceConstant;
import com.echo.ragtry.vo.ChatMessageRequest;
import com.echo.ragtry.vo.ChatMessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 智能路由服务
 * 根据配置策略在Dify和RAG之间进行智能选择
 */
@Service
@Slf4j
public class SmartRoutingService {

    @Autowired
    private DifyService difyService;

    @Autowired
    private EnhancedRAGService ragService;

    @Autowired
    private SmartRoutingConfiguration routingConfig;

    /**
     * 智能路由处理消息
     */
    public ChatMessageResponse routeMessage(ChatMessageRequest request) {
        return routeMessage(request, "default-user");
    }

    /**
     * 智能路由处理消息
     */
    public ChatMessageResponse routeMessage(ChatMessageRequest request, String userId) {
        if (!routingConfig.isEnabled()) {
            // 如果未启用智能路由，默认使用RAG
            return useRAGService(request, userId);
        }

        String strategy = routingConfig.getStrategy();
        log.info("使用路由策略: {} 处理消息: {}", strategy, request.getMessage());

        switch (strategy) {
            case CustomerServiceConstant.SmartRouting.STRATEGY_RAG_FIRST:
                return ragFirstStrategy(request, userId);

            case CustomerServiceConstant.SmartRouting.STRATEGY_DIFY_FIRST:
                return difyFirstStrategy(request, userId);

            case CustomerServiceConstant.SmartRouting.STRATEGY_PARALLEL:
                return parallelStrategy(request, userId);

            default:
                log.warn("未知的路由策略: {}, 使用RAG优先策略", strategy);
                return ragFirstStrategy(request, userId);
        }
    }

    /**
     * RAG优先策略：优先使用RAG，失败时使用Dify
     */
    private ChatMessageResponse ragFirstStrategy(ChatMessageRequest request, String userId) {
        try {
            // 首先尝试RAG
            ChatMessageResponse ragResponse = useRAGService(request, userId);
            if (isValidResponse(ragResponse)) {
                log.info("RAG优先策略：RAG响应成功");
                return ragResponse;
            }

            if (routingConfig.isFallbackEnabled()) {
                log.warn("RAG响应无效，降级到Dify服务");
                ChatMessageResponse difyResponse = useDifyService(request, userId);
                if (isValidResponse(difyResponse)) {
                    log.info("RAG优先策略：Dify降级响应成功");
                    return difyResponse;
                }
            }

            return createErrorResponse("所有服务都不可用");

        } catch (Exception e) {
            log.error("RAG优先策略执行失败", e);
            return createErrorResponse("处理请求时发生错误: " + e.getMessage());
        }
    }

    /**
     * Dify优先策略：优先使用Dify，失败时使用RAG
     */
    private ChatMessageResponse difyFirstStrategy(ChatMessageRequest request, String userId) {
        try {
            // 首先尝试Dify
            ChatMessageResponse difyResponse = useDifyService(request, userId);
            if (isValidResponse(difyResponse)) {
                log.info("Dify优先策略：Dify响应成功");
                return difyResponse;
            }

            if (routingConfig.isFallbackEnabled()) {
                log.warn("Dify响应无效，降级到RAG服务");
                ChatMessageResponse ragResponse = useRAGService(request, userId);
                if (isValidResponse(ragResponse)) {
                    log.info("Dify优先策略：RAG降级响应成功");
                    return ragResponse;
                }
            }

            return createErrorResponse("所有服务都不可用");

        } catch (Exception e) {
            log.error("Dify优先策略执行失败", e);
            return createErrorResponse("处理请求时发生错误: " + e.getMessage());
        }
    }

    /**
     * 并行策略：同时调用Dify和RAG，返回最先成功的结果
     */
    private ChatMessageResponse parallelStrategy(ChatMessageRequest request, String userId) {
        try {
            log.info("开始并行调用Dify和RAG服务");

            // 并行调用两个服务
            CompletableFuture<ChatMessageResponse> difyFuture = CompletableFuture.supplyAsync(
                    () -> {
                        try {
                            return useDifyService(request, userId);
                        } catch (Exception e) {
                            log.warn("并行策略：Dify调用失败", e);
                            return null;
                        }
                    });

            CompletableFuture<ChatMessageResponse> ragFuture = CompletableFuture.supplyAsync(
                    () -> {
                        try {
                            return useRAGService(request, userId);
                        } catch (Exception e) {
                            log.warn("并行策略：RAG调用失败", e);
                            return null;
                        }
                    });

            // 等待任意一个完成
            CompletableFuture<Object> anyOf = CompletableFuture.anyOf(difyFuture, ragFuture);

            try {
                anyOf.get(routingConfig.getTimeout(), TimeUnit.SECONDS);
            } catch (Exception e) {
                log.warn("并行调用超时", e);
            }

            // 检查结果
            ChatMessageResponse difyResult = difyFuture.isDone() ? difyFuture.get() : null;
            ChatMessageResponse ragResult = ragFuture.isDone() ? ragFuture.get() : null;

            // 选择最佳结果
            if (isValidResponse(difyResult) && isValidResponse(ragResult)) {
                // 两个都成功，选择Dify结果（可以根据需要调整优先级）
                log.info("并行策略：两个服务都成功，选择Dify结果");
                return difyResult;
            } else if (isValidResponse(difyResult)) {
                log.info("并行策略：只有Dify成功");
                return difyResult;
            } else if (isValidResponse(ragResult)) {
                log.info("并行策略：只有RAG成功");
                return ragResult;
            }

            return createErrorResponse("所有服务都不可用");

        } catch (Exception e) {
            log.error("并行策略执行失败", e);
            return createErrorResponse("处理请求时发生错误: " + e.getMessage());
        }
    }

    /**
     * 使用RAG服务
     */
    private ChatMessageResponse useRAGService(ChatMessageRequest request, String userId) {
        try {
            String answer = ragService.query(request.getMessage());

            ChatMessageResponse response = new ChatMessageResponse();
            response.setAnswer(answer);
            response.setSource(CustomerServiceConstant.SmartRouting.SERVICE_RAG);
            response.setResponseMode(CustomerServiceConstant.ResponseMode.BLOCKING);

            return response;
        } catch (Exception e) {
            log.error("RAG服务调用失败", e);
            return null;
        }
    }

    /**
     * 使用Dify服务
     */
    private ChatMessageResponse useDifyService(ChatMessageRequest request, String userId) {
        try {
            return difyService.sendMessage(request, userId);
        } catch (Exception e) {
            log.error("Dify服务调用失败", e);
            return null;
        }
    }

    /**
     * 检查响应是否有效
     */
    private boolean isValidResponse(ChatMessageResponse response) {
        return response != null &&
                StringUtils.hasText(response.getAnswer()) &&
                !response.getAnswer().contains("系统暂时繁忙") &&
                !response.getAnswer().contains("无法理解");
    }

    /**
     * 创建错误响应
     */
    private ChatMessageResponse createErrorResponse(String errorMessage) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setAnswer("抱歉，" + errorMessage + "。请稍后再试或联系人工客服。");
        response.setSource("System");
        response.setResponseMode(CustomerServiceConstant.ResponseMode.BLOCKING);
        return response;
    }

    /**
     * 获取服务健康状态
     */
    public boolean checkServicesHealth() {
        boolean difyHealth = false;
        boolean ragHealth = false;

        try {
            difyHealth = difyService.checkHealth();
        } catch (Exception e) {
            log.warn("Dify健康检查失败", e);
        }

        try {
            ragHealth = ragService.checkHealth();
        } catch (Exception e) {
            log.warn("RAG健康检查失败", e);
        }

        log.info("服务健康状态 - Dify: {}, RAG: {}", difyHealth, ragHealth);
        return difyHealth || ragHealth;
    }

    /**
     * 获取推荐的路由策略
     */
    public String getRecommendedStrategy() {
        boolean difyHealth = false;
        boolean ragHealth = false;

        try {
            difyHealth = difyService.checkHealth();
            ragHealth = ragService.checkHealth();
        } catch (Exception e) {
            log.warn("健康检查失败", e);
        }

        if (difyHealth && ragHealth) {
            return CustomerServiceConstant.SmartRouting.STRATEGY_PARALLEL;
        } else if (ragHealth) {
            return CustomerServiceConstant.SmartRouting.STRATEGY_RAG_FIRST;
        } else if (difyHealth) {
            return CustomerServiceConstant.SmartRouting.STRATEGY_DIFY_FIRST;
        } else {
            return CustomerServiceConstant.SmartRouting.STRATEGY_RAG_FIRST; // 默认
        }
    }
}

