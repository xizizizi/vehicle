package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.service.CozeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/coze")
@Slf4j
@CrossOrigin
public class CozeController {

    private final CozeService cozeService;

    public CozeController(CozeService cozeService) {
        this.cozeService = cozeService;
    }

    /**
     * 聊天接口
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String userId = request.get("userId");

        log.info("收到聊天请求: message={}, userId={}", userMessage, userId);

        // 验证参数
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return buildErrorResponse("消息内容不能为空");
        }

        // 生成用户ID（如果未提供）
        if (userId == null || userId.trim().isEmpty()) {
            userId = "user_" + System.currentTimeMillis();
            log.info("生成新用户ID: {}", userId);
        }

        return cozeService.chat(userMessage, userId);
    }

    /**
     * 快速测试接口（仅发送，不等待结果）
     */
    @PostMapping("/quick-test")
    public Map<String, Object> quickTest(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String userId = request.get("userId");

        log.info("收到快速测试请求: message={}, userId={}", userMessage, userId);

        if (userMessage == null || userMessage.trim().isEmpty()) {
            return buildErrorResponse("消息内容不能为空");
        }

        if (userId == null || userId.trim().isEmpty()) {
            userId = "test_user_" + System.currentTimeMillis();
        }

        return cozeService.quickTest(userMessage, userId);
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();
        try {
            String testReply = cozeService.callCozeBot("你好，测试一下", "test_user_001");
            result.put("success", true);
            result.put("message", "测试成功");
            result.put("reply", testReply);
            result.put("reply_length", testReply.length());
        } catch (Exception e) {
            log.error("Coze 测试失败", e);
            result.put("success", false);
            result.put("message", "测试失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("service", "CozeService");
        result.put("status", "UP");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    private Map<String, Object> buildErrorResponse(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}