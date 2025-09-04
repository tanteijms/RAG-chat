package com.echo.ragtry.service;

import com.echo.ragtry.entity.QAItem;
import com.echo.ragtry.config.RAGConfiguration;
import com.echo.ragtry.constant.CustomerServiceConstant;
import com.echo.ragtry.vo.RAGResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 增强型RAG服务
 * 使用Ollama进行向量化检索和智能回答生成
 */
@Service
@Slf4j
public class EnhancedRAGService {

    @Autowired
    private MarkdownKnowledgeLoader knowledgeLoader;

    @Autowired
    private RAGConfiguration ragConfig;

    private RestTemplate restTemplate;
    private Map<String, List<Double>> embeddingCache = new HashMap<>();
    private List<QAItem> knowledgeBase = new ArrayList<>();

    /**
     * 初始化服务
     */
    @PostConstruct
    public void init() {
        log.info("初始化增强型RAG服务...");

        restTemplate = new RestTemplate();

        try {
            // 加载知识库
            knowledgeBase = knowledgeLoader.loadKnowledge();

            // 检查Ollama服务连接
            testOllamaConnection();

            // 预计算知识库向量（异步）
            CompletableFuture.runAsync(this::precomputeEmbeddings);

            log.info("增强型RAG服务初始化完成！");

        } catch (Exception e) {
            log.error("RAG服务初始化失败，将使用降级模式", e);
        }
    }

    /**
     * RAG问答 - 主要入口
     */
    public String query(String userQuestion) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("处理RAG查询: {}", userQuestion);

            // 1. 首先尝试向量化检索
            String vectorAnswer = performVectorSearch(userQuestion);
            if (vectorAnswer != null) {
                log.info("向量检索成功，耗时: {}ms", System.currentTimeMillis() - startTime);
                return vectorAnswer;
            }

            // 2. 降级到关键词匹配
            String keywordAnswer = performKeywordSearch(userQuestion);
            if (keywordAnswer != null) {
                log.info("关键词匹配成功，耗时: {}ms", System.currentTimeMillis() - startTime);
                return keywordAnswer;
            }

            // 3. 无法匹配时的默认回复
            return "抱歉，我无法理解您的问题。请尝试换个说法，或者联系人工客服获得帮助。";

        } catch (Exception e) {
            log.error("RAG查询失败", e);
            return "系统暂时繁忙，请稍后再试。";
        }
    }

    /**
     * 执行向量化检索
     */
    private String performVectorSearch(String userQuestion) {
        try {
            // 获取用户问题的向量
            List<Double> questionEmbedding = getEmbedding(userQuestion);
            if (questionEmbedding == null) {
                return null;
            }

            // 计算与知识库的相似度
            List<QAMatch> matches = new ArrayList<>();
            for (QAItem item : knowledgeBase) {
                List<Double> itemEmbedding = embeddingCache.get(item.getId());
                if (itemEmbedding != null) {
                    double similarity = calculateCosineSimilarity(questionEmbedding, itemEmbedding);
                    matches.add(new QAMatch(item, similarity));
                }
            }

            // 筛选高相似度的匹配
            List<QAMatch> topMatches = matches.stream()
                    .filter(match -> match.similarity > ragConfig.getSearch().getSimilarityThreshold())
                    .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                    .limit(ragConfig.getSearch().getMaxResults())
                    .collect(Collectors.toList());

            if (topMatches.isEmpty()) {
                return null;
            }

            // 使用最匹配的答案生成回复
            return generateRAGAnswer(userQuestion, topMatches);

        } catch (Exception e) {
            log.error("向量检索失败", e);
            return null;
        }
    }

    /**
     * 执行关键词匹配（降级方案）
     */
    private String performKeywordSearch(String userQuestion) {
        try {
            String question = userQuestion.toLowerCase();

            for (QAItem item : knowledgeBase) {
                String itemQuestion = item.getQuestion().toLowerCase();
                String itemAnswer = item.getAnswer().toLowerCase();

                // 简单的关键词匹配
                if (itemQuestion.contains(question) ||
                        question.contains(itemQuestion) ||
                        itemAnswer.contains(question)) {
                    return item.getAnswer();
                }
            }

            return null;
        } catch (Exception e) {
            log.error("关键词匹配失败", e);
            return null;
        }
    }

    /**
     * 生成RAG答案
     */
    private String generateRAGAnswer(String userQuestion, List<QAMatch> matches) {
        try {
            // 构建上下文
            StringBuilder context = new StringBuilder();
            for (QAMatch match : matches) {
                context.append("Q: ").append(match.qaItem.getQuestion()).append("\n");
                context.append("A: ").append(match.qaItem.getAnswer()).append("\n\n");
            }

            // 调用Ollama生成答案
            String prompt = buildPrompt(userQuestion, context.toString());
            return callOllamaGenerate(prompt);

        } catch (Exception e) {
            log.error("生成RAG答案失败", e);
            // 降级：直接返回最相似的答案
            return matches.get(0).qaItem.getAnswer();
        }
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(String question, String context) {
        return String.format(
                "基于以下知识库信息回答用户问题。请确保回答准确、简洁、有帮助。\n\n" +
                        "知识库信息：\n%s\n" +
                        "用户问题：%s\n\n" +
                        "回答：",
                context, question);
    }

    /**
     * 调用Ollama生成回答
     */
    private String callOllamaGenerate(String prompt) {
        try {
            String url = ragConfig.getOllama().getBaseUrl() + "/api/generate";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", ragConfig.getOllama().getChatModel());
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getBody() != null) {
                return (String) response.getBody().get("response");
            }

            return null;
        } catch (Exception e) {
            log.error("调用Ollama生成失败", e);
            return null;
        }
    }

    /**
     * 获取文本的向量表示
     */
    private List<Double> getEmbedding(String text) {
        try {
            String url = ragConfig.getOllama().getBaseUrl() + "/api/embeddings";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", ragConfig.getOllama().getEmbeddingModel());
            requestBody.put("prompt", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getBody() != null && response.getBody().get("embedding") != null) {
                List<Double> embedding = (List<Double>) response.getBody().get("embedding");
                return embedding;
            }

            return null;
        } catch (Exception e) {
            log.error("获取embedding失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 计算余弦相似度
     */
    private double calculateCosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * 测试Ollama连接
     */
    private void testOllamaConnection() {
        try {
            String healthUrl = ragConfig.getOllama().getBaseUrl() + "/api/tags";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            log.info("Ollama服务连接正常: {}", response.getStatusCode());
        } catch (Exception e) {
            log.warn("无法连接到Ollama服务: {}", e.getMessage());
        }
    }

    /**
     * 预计算知识库向量
     */
    private void precomputeEmbeddings() {
        try {
            log.info("开始预计算 {} 条知识库的向量...", knowledgeBase.size());

            for (QAItem item : knowledgeBase) {
                String content = item.getQuestion() + " " + item.getAnswer();
                List<Double> embedding = getEmbedding(content);
                if (embedding != null) {
                    embeddingCache.put(item.getId(), embedding);
                }

                // 避免请求过快
                Thread.sleep(ragConfig.getAsync().getPrecomputeDelay());
            }

            log.info("知识库向量预计算完成，共缓存 {} 个向量", embeddingCache.size());

        } catch (Exception e) {
            log.error("预计算向量失败", e);
        }
    }

    /**
     * 重新加载知识库
     */
    public void reloadKnowledge() {
        try {
            log.info("重新加载知识库...");
            knowledgeBase = knowledgeLoader.loadKnowledge();
            embeddingCache.clear();

            // 异步重新计算向量
            CompletableFuture.runAsync(this::precomputeEmbeddings);

            log.info("知识库重新加载完成");
        } catch (Exception e) {
            log.error("重新加载知识库失败", e);
        }
    }

    /**
     * 检查RAG服务健康状态
     */
    public boolean checkHealth() {
        try {
            // 测试Ollama连接
            testOllamaConnection();

            // 测试简单查询
            String testAnswer = query("测试");
            return testAnswer != null && !testAnswer.contains("系统暂时繁忙");
        } catch (Exception e) {
            log.warn("RAG健康检查失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * QA匹配结果类
     */
    private static class QAMatch {
        final QAItem qaItem;
        final double similarity;

        QAMatch(QAItem qaItem, double similarity) {
            this.qaItem = qaItem;
            this.similarity = similarity;
        }
    }
}

