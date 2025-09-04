package com.echo.ragtry.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * RAG查询请求对象
 */
@Data
public class RAGRequest {

    @NotBlank(message = "问题不能为空")
    private String question;
}

