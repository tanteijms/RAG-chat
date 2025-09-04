package com.echo.ragtry.vo;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 聊天消息请求VO
 */
@Data
public class ChatMessageRequest {

    @NotBlank(message = "消息内容不能为空")
    private String message;

    private String conversationId;

    private String user;
}

