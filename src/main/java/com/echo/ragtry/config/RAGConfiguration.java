package com.echo.ragtry.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RAG服务配置类
 * 映射application.yml中的rag配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "rag")
public class RAGConfiguration {

    /**
     * Ollama相关配置
     */
    private Ollama ollama = new Ollama();

    /**
     * 搜索相关配置
     */
    private Search search = new Search();

    /**
     * 异步处理配置
     */
    private Async async = new Async();

    /**
     * 知识库配置
     */
    private Knowledge knowledge = new Knowledge();

    @Data
    public static class Ollama {
        /**
         * Ollama服务地址
         */
        private String baseUrl = "http://localhost:11434";

        /**
         * 向量化模型名称
         */
        private String embeddingModel = "nomic-embed-text";

        /**
         * 对话生成模型名称
         */
        private String chatModel = "qwen2:7b";

        /**
         * 请求超时时间(秒)
         */
        private int timeout = 120;

        /**
         * 最大重试次数
         */
        private int maxRetries = 3;
    }

    @Data
    public static class Search {
        /**
         * 相似度阈值
         */
        private double similarityThreshold = 0.7;

        /**
         * 最大返回结果数
         */
        private int maxResults = 5;

        /**
         * 向量缓存大小
         */
        private int embeddingCacheSize = 1000;
    }

    @Data
    public static class Async {
        /**
         * 预计算向量的延迟(毫秒)
         */
        private long precomputeDelay = 1000;

        /**
         * 健康检查间隔(秒)
         */
        private long healthCheckInterval = 30;
    }

    @Data
    public static class Knowledge {
        /**
         * 知识库基础路径
         */
        private String basePath = "knowledge";

        /**
         * 是否自动重载
         */
        private boolean autoReload = true;
    }
}

