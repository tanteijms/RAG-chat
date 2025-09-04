package com.echo.ragtry.vo;

import lombok.Builder;
import lombok.Data;

/**
 * RAG查询响应对象
 */
@Data
@Builder
public class RAGResponse {

    private String answer;

    private String source;

    private Double confidence;

    private Long responseTime;

    private String status;
}

