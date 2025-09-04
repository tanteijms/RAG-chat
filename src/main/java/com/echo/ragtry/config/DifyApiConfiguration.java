package com.echo.ragtry.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Dify API配置类
 */
@Configuration
public class DifyApiConfiguration {

    /**
     * 创建用于调用Dify API的WebClient
     */
    @Bean
    public WebClient difyWebClient(DifyApiProperties difyApiProperties) {
        return WebClient.builder()
                .baseUrl(difyApiProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + difyApiProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
    }

    /**
     * Dify API配置属性
     */
    @Data
    @Configuration
    @ConfigurationProperties(prefix = "dify.api")
    public static class DifyApiProperties {

        /**
         * Dify API基础URL
         */
        private String baseUrl = "http://localhost/v1";

        /**
         * Dify API密钥
         */
        private String apiKey;

        /**
         * 请求超时时间（秒）
         */
        private int timeout = 30;

        /**
         * 用户标识
         */
        private String defaultUser = "rag-try-user";

        /**
         * 是否启用调试模式
         */
        private boolean debug = false;
    }
}

