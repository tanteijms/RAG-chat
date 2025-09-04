package com.echo.ragtry.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 智能路由配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "smart-routing")
public class SmartRoutingConfiguration {

    /**
     * 是否启用智能路由
     */
    private boolean enabled = true;

    /**
     * 路由策略：rag-first, dify-first, parallel
     */
    private String strategy = "rag-first";

    /**
     * 是否启用降级机制
     */
    private boolean fallbackEnabled = true;

    /**
     * 超时时间（秒）
     */
    private int timeout = 30;
}

