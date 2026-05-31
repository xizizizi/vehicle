package io.vehicle.vehicle_admin.service;

import io.vehicle.vehicle_admin.exception.CozeApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class CozeService {

    @Value("${coze.api-key}")
    private String apiKey;

    @Value("${coze.bot-id}")
    private String botId;

    @Value("${coze.api-chat-url}")
    private String chatUrl;

    @Value("${coze.api-retrieve-url}")
    private String retrieveUrl;

    @Value("${coze.api-message-list-url}")
    private String messageListUrl;

    @Value("${coze.max-attempts:30}")
    private int maxAttempts;

    @Value("${coze.poll-interval:2000}")
    private int pollInterval;

    // 使用专用的 RestTemplate
    private final RestTemplate cozeRestTemplate;

    public CozeService(@Qualifier("cozeRestTemplate") RestTemplate cozeRestTemplate) {
        this.cozeRestTemplate = cozeRestTemplate;
    }

    /**
     * 调用 Coze 智能体（三步法）
     */
    public String callCozeBot(String userMessage, String userId) {
        // 步骤1: 发送消息
        Map<String, Object> responseData = sendMessage(userMessage, userId);
        String conversationId = (String) responseData.get("conversation_id");
        String chatId = (String) responseData.get("id");

        // 步骤2: 轮询查询状态
        pollForCompletion(conversationId, chatId);

        // 步骤3: 获取最终结果
        return getFinalResult(conversationId, chatId);
    }

    /**
     * 步骤1: 发送消息
     */
    private Map<String, Object> sendMessage(String userMessage, String userId) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Accept", "application/json");

        // 构建请求体
        Map<String, Object> request = new HashMap<>();
        request.put("bot_id", botId);
        request.put("user_id", userId);
        request.put("stream", false);

        // 构建消息
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("content", userMessage);
        message.put("content_type", "text");
        message.put("role", "user");
        message.put("type", "question");
        messages.add(message);
        request.put("additional_messages", messages);
        request.put("parameters", new HashMap<>());

        // 发送请求
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        log.debug("发送消息到 Coze: {}", request);

        try {
            ResponseEntity<Map> response = cozeRestTemplate.exchange(
                    chatUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            log.debug("Coze 响应: {}", responseBody);

            if (responseBody == null || !responseBody.containsKey("data")) {
                throw new CozeApiException("发送消息失败: 响应数据为空或不完整");
            }

            Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
            String conversationId = (String) data.get("conversation_id");
            String chatId = (String) data.get("id");

            log.info("发送消息成功，conversation_id: {}, chat_id: {}", conversationId, chatId);
            return data;

        } catch (Exception e) {
            log.error("发送消息到 Coze 失败", e);
            throw new CozeApiException("发送消息到 Coze 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 步骤2: 轮询查询状态
     */
    private void pollForCompletion(String conversationId, String chatId) {
        log.info("开始轮询 Coze 状态, conversation_id: {}, chat_id: {}", conversationId, chatId);

        for (int i = 0; i < maxAttempts; i++) {
            try {
                // 等待间隔
                TimeUnit.MILLISECONDS.sleep(pollInterval);

                // 查询状态
                Map<String, Object> statusData = retrieveStatus(conversationId, chatId);
                String status = (String) statusData.get("status");

                log.debug("第{}次查询状态: {}", i + 1, status);

                if ("completed".equals(status)) {
                    log.info("智能体已完成生成");
                    return;
                } else if ("failed".equals(status)) {
                    throw new CozeApiException("智能体生成失败");
                } else if ("stopped".equals(status)) {
                    throw new CozeApiException("智能体生成被停止");
                }
                // 继续等待...

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CozeApiException("轮询被中断", e);
            }
        }

        throw new CozeApiException("等待智能体响应超时（最大尝试次数: " + maxAttempts + "）");
    }

    /**
     * 查询状态
     */
    private Map<String, Object> retrieveStatus(String conversationId, String chatId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Accept", "application/json");

        // 构建查询参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(retrieveUrl)
                .queryParam("conversation_id", conversationId)
                .queryParam("chat_id", chatId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = cozeRestTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("data")) {
                throw new CozeApiException("查询状态失败: 响应数据为空或不完整");
            }

            return (Map<String, Object>) responseBody.get("data");

        } catch (Exception e) {
            log.error("查询 Coze 状态失败", e);
            throw new CozeApiException("查询 Coze 状态失败: " + e.getMessage(), e);
        }
    }

    /**
     * 步骤3: 获取最终结果
     */
    private String getFinalResult(String conversationId, String chatId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Accept", "application/json");

        // 构建查询参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(messageListUrl)
                .queryParam("conversation_id", conversationId)
                .queryParam("chat_id", chatId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = cozeRestTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("data")) {
                throw new CozeApiException("获取结果失败: 响应数据为空或不完整");
            }

            // 解析消息列表，找到助手回复
            List<Map<String, Object>> messages = (List<Map<String, Object>>) responseBody.get("data");
            for (Map<String, Object> message : messages) {
                if ("answer".equals(message.get("type")) && "assistant".equals(message.get("role"))) {
                    String content = (String) message.get("content");
                    log.info("获取到助手回复，长度: {}", content.length());
                    return content;
                }
            }

            // 如果没有找到助手回复，返回第一条非用户消息
            for (Map<String, Object> message : messages) {
                if (!"user".equals(message.get("role"))) {
                    String content = (String) message.get("content");
                    log.warn("未找到标准助手回复，使用第一条非用户消息，长度: {}", content.length());
                    return content;
                }
            }

            throw new CozeApiException("未找到有效的回复消息");

        } catch (Exception e) {
            log.error("获取 Coze 结果失败", e);
            throw new CozeApiException("获取 Coze 结果失败: " + e.getMessage(), e);
        }
    }

    /**
     * 简化调用方法（为前端提供）
     */
    public Map<String, Object> chat(String userMessage, String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            String reply = callCozeBot(userMessage, userId);
            result.put("success", true);
            result.put("data", reply);
            result.put("timestamp", System.currentTimeMillis());
        } catch (CozeApiException e) {
            log.error("调用 Coze API 失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            log.error("调用 Coze 服务发生未知错误", e);
            result.put("success", false);
            result.put("message", "服务内部错误: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        return result;
    }

    /**
     * 快速测试方法（无需轮询）
     */
    public Map<String, Object> quickTest(String userMessage, String userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> responseData = sendMessage(userMessage, userId);
            result.put("success", true);
            result.put("message", "消息发送成功");
            result.put("conversation_id", responseData.get("conversation_id"));
            result.put("chat_id", responseData.get("id"));
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
        }
        return result;
    }
}