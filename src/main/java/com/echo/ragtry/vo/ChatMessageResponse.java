package com.echo.ragtry.vo;

import lombok.Data;

/**
 * 聊天消息响应VO
 */
@Data
public class ChatMessageResponse {

    private String conversationId;

    private String messageId;

    private String taskId;

    private String webSocketId;

    private String answer;

    private String responseMode;

    private String source; // 来源：RAG、Dify、Keyword
}

