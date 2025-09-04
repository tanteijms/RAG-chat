package com.echo.ragtry.service;

import com.echo.ragtry.entity.QAItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown知识库加载器
 * 从MD文档中解析QA对
 */
@Component
@Slf4j
public class MarkdownKnowledgeLoader {

    private static final String KNOWLEDGE_FILE = "knowledge/bot.md";
    private static final Pattern CATEGORY_PATTERN = Pattern.compile("^## (.+)$");
    private static final Pattern QUESTION_PATTERN = Pattern.compile("^### Q: (.+)$");
    private static final Pattern ANSWER_PATTERN = Pattern.compile("^\\*\\*A:\\*\\* (.+)$");

    /**
     * 从MD文档加载知识库
     */
    public List<QAItem> loadKnowledge() {
        List<QAItem> qaItems = new ArrayList<>();

        try {
            String content = readMarkdownFile();
            qaItems = parseMarkdownContent(content);

            log.info("从MD文档加载知识库完成，共解析 {} 条QA对", qaItems.size());

        } catch (Exception e) {
            log.error("加载MD知识库失败，使用默认知识库", e);
            // 返回默认知识库，避免系统崩溃
            qaItems = getDefaultKnowledge();
        }

        return qaItems;
    }

    /**
     * 读取MD文件内容
     */
    private String readMarkdownFile() throws IOException {
        try {
            ClassPathResource resource = new ClassPathResource(KNOWLEDGE_FILE);
            StringBuilder content = new StringBuilder();

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }

            log.debug("成功从classpath读取文件: {}", KNOWLEDGE_FILE);
            return content.toString();

        } catch (Exception e) {
            log.error("从classpath读取MD文件失败", e);
            throw new IOException("无法读取知识库文件: " + KNOWLEDGE_FILE, e);
        }
    }

    /**
     * 解析MD内容为QA对
     */
    private List<QAItem> parseMarkdownContent(String content) {
        List<QAItem> qaItems = new ArrayList<>();
        String[] lines = content.split("\n");

        String currentCategory = "通用";
        String currentQuestion = null;
        StringBuilder currentAnswer = new StringBuilder();
        int qaCounter = 1;

        for (String line : lines) {
            line = line.trim();

            // 匹配分类
            Matcher categoryMatcher = CATEGORY_PATTERN.matcher(line);
            if (categoryMatcher.matches()) {
                currentCategory = categoryMatcher.group(1);
                continue;
            }

            // 匹配问题
            Matcher questionMatcher = QUESTION_PATTERN.matcher(line);
            if (questionMatcher.matches()) {
                // 保存上一个QA对
                if (currentQuestion != null && currentAnswer.length() > 0) {
                    QAItem item = new QAItem(
                            "qa_" + String.format("%03d", qaCounter++),
                            currentQuestion,
                            currentAnswer.toString().trim(),
                            currentCategory);
                    qaItems.add(item);
                }

                // 开始新的问题
                currentQuestion = questionMatcher.group(1);
                currentAnswer = new StringBuilder();
                continue;
            }

            // 匹配答案开始
            Matcher answerMatcher = ANSWER_PATTERN.matcher(line);
            if (answerMatcher.matches()) {
                currentAnswer.append(answerMatcher.group(1));
                continue;
            }

            // 继续答案内容（非空行且不是标题）
            if (currentQuestion != null && !line.isEmpty() &&
                    !line.startsWith("#") && !line.startsWith("**A:**")) {
                if (currentAnswer.length() > 0) {
                    currentAnswer.append("\n");
                }
                currentAnswer.append(line);
            }
        }

        // 保存最后一个QA对
        if (currentQuestion != null && currentAnswer.length() > 0) {
            QAItem item = new QAItem(
                    "qa_" + String.format("%03d", qaCounter),
                    currentQuestion,
                    currentAnswer.toString().trim(),
                    currentCategory);
            qaItems.add(item);
        }

        return qaItems;
    }

    /**
     * 获取默认知识库（当无法读取MD文件时使用）
     */
    private List<QAItem> getDefaultKnowledge() {
        List<QAItem> defaultItems = new ArrayList<>();

        defaultItems.add(new QAItem("qa_001", "什么是RAG？",
                "RAG（Retrieval-Augmented Generation）是检索增强生成技术，结合了信息检索和文本生成能力。", "技术"));

        defaultItems.add(new QAItem("qa_002", "如何使用这个系统？",
                "您可以通过发送消息与我对话，我会基于知识库为您提供准确的回答。", "使用指南"));

        defaultItems.add(new QAItem("qa_003", "系统支持哪些功能？",
                "系统支持Dify API调用、本地RAG问答、智能路由等功能。", "功能介绍"));

        return defaultItems;
    }

    /**
     * 重新加载知识库
     */
    public void reloadKnowledge() {
        log.info("正在重新加载知识库...");
        // 这里可以添加缓存清理等逻辑
    }
}

