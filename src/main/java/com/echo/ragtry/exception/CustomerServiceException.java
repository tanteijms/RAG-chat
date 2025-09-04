package com.echo.ragtry.exception;

import com.echo.ragtry.constant.CustomerServiceConstant;

/**
 * 客服系统业务异常
 */
public class CustomerServiceException extends RuntimeException {

    private String errorCode;

    public CustomerServiceException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CustomerServiceException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    /**
     * 消息为空异常
     */
    public static CustomerServiceException messageEmpty() {
        return new CustomerServiceException(
                CustomerServiceConstant.ErrorCode.MESSAGE_EMPTY,
                "消息内容不能为空");
    }

    /**
     * 消息过长异常
     */
    public static CustomerServiceException messageTooLong() {
        return new CustomerServiceException(
                CustomerServiceConstant.ErrorCode.MESSAGE_TOO_LONG,
                "消息内容过长，最大长度为" + CustomerServiceConstant.Config.MAX_MESSAGE_LENGTH + "字符");
    }

    /**
     * 用户未认证异常
     */
    public static CustomerServiceException userNotAuthenticated() {
        return new CustomerServiceException(
                CustomerServiceConstant.ErrorCode.USER_NOT_AUTHENTICATED,
                "用户未认证，请先登录");
    }

    /**
     * API调用失败异常
     */
    public static CustomerServiceException apiCallFailed(String message) {
        return new CustomerServiceException(
                CustomerServiceConstant.ErrorCode.API_CALL_FAILED,
                "API调用失败：" + message);
    }

    /**
     * 会话不存在异常
     */
    public static CustomerServiceException conversationNotFound(String conversationId) {
        return new CustomerServiceException(
                CustomerServiceConstant.ErrorCode.CONVERSATION_NOT_FOUND,
                "会话不存在：" + conversationId);
    }

    /**
     * 任务不存在异常
     */
    public static CustomerServiceException taskNotFound(String taskId) {
        return new CustomerServiceException(
                CustomerServiceConstant.ErrorCode.TASK_NOT_FOUND,
                "任务不存在：" + taskId);
    }
}

