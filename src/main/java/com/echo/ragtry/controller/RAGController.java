package com.echo.ragtry.controller;

import com.echo.ragtry.service.EnhancedRAGService;
import com.echo.ragtry.vo.RAGRequest;
import com.echo.ragtry.vo.RAGResponse;
import com.echo.ragtry.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * RAG控制器
 * 提供直接调用RAG服务的接口
 */
@RestController
@RequestMapping("/api/rag")
@Slf4j
@CrossOrigin(origins = "*")
public class RAGController {

    @Autowired
    private EnhancedRAGService ragService;

    /**
     * RAG问答接口
     */
    @PostMapping("/query")
    public Result<RAGResponse> query(@Valid @RequestBody RAGRequest request) {
        log.info("收到RAG查询: {}", request.getQuestion());

        long startTime = System.currentTimeMillis();

        try {
            String answer = ragService.query(request.getQuestion());
            long responseTime = System.currentTimeMillis() - startTime;

            RAGResponse response = RAGResponse.builder()
                    .answer(answer)
                    .source("RAG")
                    .responseTime(responseTime)
                    .status("success")
                    .build();

            return Result.success(response);
        } catch (Exception e) {
            log.error("RAG查询失败", e);

            RAGResponse response = RAGResponse.builder()
                    .answer("抱歉，查询过程中出现错误，请稍后再试。")
                    .source("RAG")
                    .responseTime(System.currentTimeMillis() - startTime)
                    .status("error")
                    .build();

            return Result.success(response);
        }
    }

    /**
     * 重新加载知识库
     */
    @PostMapping("/reload")
    public Result<String> reloadKnowledge() {
        log.info("重新加载RAG知识库");

        try {
            ragService.reloadKnowledge();
            return Result.success("知识库重新加载成功");
        } catch (Exception e) {
            log.error("重新加载知识库失败", e);
            return Result.error("重新加载知识库失败: " + e.getMessage());
        }
    }

    /**
     * RAG服务健康检查
     */
    @GetMapping("/health")
    public Result<String> checkHealth() {
        try {
            boolean isHealthy = ragService.checkHealth();
            if (isHealthy) {
                return Result.success("RAG服务运行正常");
            } else {
                return Result.error(503, "RAG服务不可用");
            }
        } catch (Exception e) {
            log.error("RAG健康检查失败", e);
            return Result.error("RAG健康检查失败: " + e.getMessage());
        }
    }
}

