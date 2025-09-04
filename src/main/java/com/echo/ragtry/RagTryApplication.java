package com.echo.ragtry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * RAG-Try 智能客服系统启动类
 * 支持Dify API和本地Ollama RAG功能
 */
@SpringBootApplication
@EnableAsync
public class RagTryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagTryApplication.class, args);
        System.out.println("\n====================================");
        System.out.println("🤖 RAG-Try 智能客服系统启动成功！");
        System.out.println("📚 支持功能：");
        System.out.println("   - Dify API 客服对话");
        System.out.println("   - 本地 Ollama RAG 问答");
        System.out.println("   - 智能路由与降级机制");
        System.out.println("🔗 接口文档：http://localhost:8080/swagger-ui.html");
        System.out.println("====================================\n");
    }
}

