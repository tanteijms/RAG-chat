package com.echo.ragtry.constant;

/**
 * 客服系统相关常量
 * 
 * @author RAG-Try Team
 * @version 1.0
 */
public class CustomerServiceConstant {

    /**
     * Dify API事件类型
     */
    public static final class DifyEventType {
        /** 普通消息 */
        public static final String MESSAGE = "message";
        /** Agent消息 */
        public static final String AGENT_MESSAGE = "agent_message";
        /** Agent思考 */
        public static final String AGENT_THOUGHT = "agent_thought";
        /** 消息结束 */
        public static final String MESSAGE_END = "message_end";
        /** 消息文件 */
        public static final String MESSAGE_FILE = "message_file";
        /** 错误 */
        public static final String ERROR = "error";
        /** 心跳 */
        public static final String PING = "ping";
        /** TTS消息 */
        public static final String TTS_MESSAGE = "tts_message";
        /** TTS消息结束 */
        public static final String TTS_MESSAGE_END = "tts_message_end";
        /** 消息替换 */
        public static final String MESSAGE_REPLACE = "message_replace";
    }

    /**
     * 响应模式
     */
    public static final class ResponseMode {
        /** 流式模式 */
        public static final String STREAMING = "streaming";
        /** 阻塞模式 */
        public static final String BLOCKING = "blocking";
    }

    /**
     * 会话状态
     */
    public static final class ConversationStatus {
        /** 活跃 */
        public static final Integer ACTIVE = 1;
        /** 已结束 */
        public static final Integer ENDED = 0;
    }

    /**
     * 消息发送者类型
     */
    public static final class SenderType {
        /** 用户 */
        public static final Integer USER = 1;
        /** AI客服 */
        public static final Integer AI = 2;
    }

    /**
     * 内容类型
     */
    public static final class ContentType {
        /** 文本 */
        public static final Integer TEXT = 1;
        /** 图片 */
        public static final Integer IMAGE = 2;
        /** 文件 */
        public static final Integer FILE = 3;
    }

    /**
     * 系统配置
     */
    public static final class Config {
        /** 最大消息长度 */
        public static final Integer MAX_MESSAGE_LENGTH = 2000;
        /** 默认用户标识前缀 */
        public static final String DEFAULT_USER_PREFIX = "rag-try-user-";
        /** 会话超时时间（毫秒） */
        public static final Long CONVERSATION_TIMEOUT = 30 * 60 * 1000L; // 30分钟
        /** 流式响应超时时间（毫秒） */
        public static final Long STREAMING_TIMEOUT = 60 * 1000L; // 1分钟
    }

    /**
     * API路径
     */
    public static final class ApiPath {
        /** 发送消息 */
        public static final String CHAT_MESSAGES = "/chat-messages";
        /** 停止响应 */
        public static final String STOP_STREAMING = "/chat-messages/{taskId}/stop";
        /** 文件上传 */
        public static final String FILE_UPLOAD = "/files/upload";
        /** 消息反馈 */
        public static final String MESSAGE_FEEDBACK = "/messages/{messageId}/feedbacks";
        /** 获取会话历史 */
        public static final String CONVERSATION_MESSAGES = "/messages";
        /** 获取会话列表 */
        public static final String CONVERSATIONS = "/conversations";
    }

    /**
     * 错误代码
     */
    public static final class ErrorCode {
        /** 用户未认证 */
        public static final String USER_NOT_AUTHENTICATED = "USER_NOT_AUTHENTICATED";
        /** 消息为空 */
        public static final String MESSAGE_EMPTY = "MESSAGE_EMPTY";
        /** 消息过长 */
        public static final String MESSAGE_TOO_LONG = "MESSAGE_TOO_LONG";
        /** API调用失败 */
        public static final String API_CALL_FAILED = "API_CALL_FAILED";
        /** 会话不存在 */
        public static final String CONVERSATION_NOT_FOUND = "CONVERSATION_NOT_FOUND";
        /** 任务不存在 */
        public static final String TASK_NOT_FOUND = "TASK_NOT_FOUND";
        /** 权限不足 */
        public static final String INSUFFICIENT_PERMISSION = "INSUFFICIENT_PERMISSION";
    }

    /**
     * 快捷回复模板
     */
    public static final class QuickReplies {
        /** VIP会员服务 */
        public static final String VIP_SERVICE = "我想了解VIP会员服务";
        /** 购买音乐 */
        public static final String PURCHASE_MUSIC = "如何购买音乐专辑";
        /** 播放问题 */
        public static final String PLAYBACK_ISSUE = "播放遇到问题";
        /** 账户问题 */
        public static final String ACCOUNT_ISSUE = "账户相关问题";
        /** 其他问题 */
        public static final String OTHER_ISSUE = "其他问题";
    }

    /**
     * 默认消息模板
     */
    public static final class MessageTemplate {
        /** 欢迎消息 */
        public static final String WELCOME = "您好！我是RAG-Try智能客服，有什么可以帮助您的吗？";
        /** 会话开始 */
        public static final String CONVERSATION_START = "新的客服会话已开始";
        /** 会话结束 */
        public static final String CONVERSATION_END = "客服会话已结束，感谢您的使用";
        /** 系统繁忙 */
        public static final String SYSTEM_BUSY = "系统繁忙，请稍后再试";
        /** 网络错误 */
        public static final String NETWORK_ERROR = "网络连接出现问题，请检查网络后重试";
    }

    /**
     * 智能路由相关常量
     */
    public static final class SmartRouting {
        /** RAG优先策略 */
        public static final String STRATEGY_RAG_FIRST = "rag-first";
        /** Dify优先策略 */
        public static final String STRATEGY_DIFY_FIRST = "dify-first";
        /** 并行策略 */
        public static final String STRATEGY_PARALLEL = "parallel";

        /** 服务类型：RAG */
        public static final String SERVICE_RAG = "RAG";
        /** 服务类型：Dify */
        public static final String SERVICE_DIFY = "Dify";
        /** 服务类型：关键词匹配 */
        public static final String SERVICE_KEYWORD = "Keyword";
    }
}

