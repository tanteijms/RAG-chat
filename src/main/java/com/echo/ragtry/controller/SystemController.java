package com.echo.ragtry.controller;

import com.echo.ragtry.config.DifyApiConfiguration;
import com.echo.ragtry.config.RAGConfiguration;
import com.echo.ragtry.config.SmartRoutingConfiguration;
import com.echo.ragtry.service.DifyService;
import com.echo.ragtry.service.EnhancedRAGService;
import com.echo.ragtry.service.SmartRoutingService;
import com.echo.ragtry.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统控制器
 * 提供系统信息、配置和综合测试接口
 */
@RestController
@RequestMapping("/api/system")
@Slf4j
@CrossOrigin(origins = "*")
public class SystemController {

    @Autowired
    private DifyService difyService;

    @Autowired
    private EnhancedRAGService ragService;

    @Autowired
    private SmartRoutingService routingService;

    @Autowired
    private DifyApiConfiguration.DifyApiProperties difyConfig;

    @Autowired
    private RAGConfiguration ragConfig;

    @Autowired
    private SmartRoutingConfiguration routingConfig;

    /**
     * 系统信息
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("name", "RAG-Try 智能客服系统");
        info.put("version", "1.0.0");
        info.put("description", "支持Dify API和本地Ollama RAG的智能客服系统");

        // 配置信息
        Map<String, Object> config = new HashMap<>();
        config.put("dify", Map.of(
                "baseUrl", difyConfig.getBaseUrl(),
                "timeout", difyConfig.getTimeout(),
                "debug", difyConfig.isDebug()));
        config.put("rag", Map.of(
                "ollamaBaseUrl", ragConfig.getOllama().getBaseUrl(),
                "embeddingModel", ragConfig.getOllama().getEmbeddingModel(),
                "chatModel", ragConfig.getOllama().getChatModel(),
                "similarityThreshold", ragConfig.getSearch().getSimilarityThreshold()));
        config.put("routing", Map.of(
                "enabled", routingConfig.isEnabled(),
                "strategy", routingConfig.getStrategy(),
                "fallbackEnabled", routingConfig.isFallbackEnabled()));
        info.put("config", config);

        return Result.success(info);
    }

    /**
     * 综合健康检查
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> checkSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        boolean difyHealth = false;
        boolean ragHealth = false;
        boolean overallHealth = false;

        try {
            difyHealth = difyService.checkHealth();
        } catch (Exception e) {
            log.warn("Dify健康检查异常", e);
        }

        try {
            ragHealth = ragService.checkHealth();
        } catch (Exception e) {
            log.warn("RAG健康检查异常", e);
        }

        overallHealth = difyHealth || ragHealth;

        health.put("overall", overallHealth);
        health.put("services", Map.of(
                "dify", difyHealth,
                "rag", ragHealth));
        health.put("recommendedStrategy", routingService.getRecommendedStrategy());

        if (overallHealth) {
            return Result.success(health);
        } else {
            Result<Map<String, Object>> result = Result.error(503, "系统服务不可用");
            result.setData(health);
            return result;
        }
    }

    /**
     * 系统状态
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();

        // 运行时信息
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> runtime_info = new HashMap<>();
        runtime_info.put("totalMemory", runtime.totalMemory() / 1024 / 1024 + " MB");
        runtime_info.put("freeMemory", runtime.freeMemory() / 1024 / 1024 + " MB");
        runtime_info.put("maxMemory", runtime.maxMemory() / 1024 / 1024 + " MB");
        runtime_info.put("processors", runtime.availableProcessors());

        status.put("runtime", runtime_info);
        status.put("timestamp", System.currentTimeMillis());
        status.put("uptime", java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime());

        return Result.success(status);
    }

    /**
     * 快速测试接口
     */
    @PostMapping("/test")
    public Result<Map<String, Object>> quickTest(@RequestParam(defaultValue = "你好") String message) {
        log.info("执行快速测试，消息: {}", message);

        Map<String, Object> testResults = new HashMap<>();

        // 测试Dify
        try {
            long startTime = System.currentTimeMillis();
            boolean difyResult = difyService.checkHealth();
            long difyTime = System.currentTimeMillis() - startTime;

            testResults.put("dify", Map.of(
                    "available", difyResult,
                    "responseTime", difyTime + "ms"));
        } catch (Exception e) {
            testResults.put("dify", Map.of(
                    "available", false,
                    "error", e.getMessage()));
        }

        // 测试RAG
        try {
            long startTime = System.currentTimeMillis();
            String ragAnswer = ragService.query(message);
            long ragTime = System.currentTimeMillis() - startTime;

            testResults.put("rag", Map.of(
                    "available", true,
                    "responseTime", ragTime + "ms",
                    "answer",
                    ragAnswer != null ? ragAnswer.substring(0, Math.min(100, ragAnswer.length())) + "..." : "无回答"));
        } catch (Exception e) {
            testResults.put("rag", Map.of(
                    "available", false,
                    "error", e.getMessage()));
        }

        return Result.success(testResults);
    }

    /**
     * 获取API文档链接
     */
    @GetMapping("/docs")
    public Result<Map<String, String>> getApiDocs() {
        Map<String, String> docs = new HashMap<>();
        docs.put("chat", "POST /api/chat/send - 智能聊天接口");
        docs.put("dify", "POST /api/dify/chat - 直接调用Dify");
        docs.put("rag", "POST /api/rag/query - 直接调用RAG");
        docs.put("health", "GET /api/system/health - 系统健康检查");
        docs.put("info", "GET /api/system/info - 系统信息");
        docs.put("test", "POST /api/system/test - 快速测试");

        return Result.success(docs);
    }
}
