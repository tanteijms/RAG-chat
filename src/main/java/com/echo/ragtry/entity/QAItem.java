package com.echo.ragtry.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * QA问答对数据模型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QAItem {

    /**
     * QA对唯一标识
     */
    private String id;

    /**
     * 问题内容
     */
    private String question;

    /**
     * 答案内容
     */
    private String answer;

    /**
     * 知识分类
     */
    private String category;
}

