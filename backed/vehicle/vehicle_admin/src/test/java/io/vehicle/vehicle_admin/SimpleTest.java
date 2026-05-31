package io.vehicle.vehicle_admin;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
// SimpleTest.java - 可以直接运行的测试
public class SimpleTest {
    public static void main(String[] args) throws Exception {
        testCozeAPI();
    }

    public static void testCozeAPI() throws Exception {
        // 从您的配置中获取
        String apiKey = "pat_dQzLSAZ5PkuQlPrZprFdmMCDtJy8QqAqvfPq3STBGjh8evz8iBYWWNY";
        String botId = "7598031741319938099";
        String workspaceId = "7573602611782680616";

        System.out.println("=== 测试扣子API ===");
        System.out.println("API Key: " + apiKey.substring(0, 10) + "...");
        System.out.println("Bot ID: " + botId);
        System.out.println("Workspace ID: " + workspaceId);

        OkHttpClient client = new OkHttpClient();

        // 构建请求
        String json = String.format(
                "{\"bot_id\":\"%s\",\"user_id\":\"test_user_123\",\"query\":\"你好，测试\",\"stream\":false}",
                botId
        );

        Request request = new Request.Builder()
                .url("https://api.coze.cn/v1/chat")
                .post(RequestBody.create(
                        okhttp3.MediaType.parse("application/json; charset=utf-8"),
                        json
                ))
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Workspace-Id", workspaceId)
                .build();

        System.out.println("\n发送请求...");

        try (Response response = client.newCall(request).execute()) {
            System.out.println("响应状态码: " + response.code());
            System.out.println("响应消息: " + response.message());

            if (response.body() != null) {
                String responseBody = response.body().string();
                System.out.println("响应体: " + responseBody);

                if (response.isSuccessful()) {
                    System.out.println("\n✅ API调用成功！");
                } else {
                    System.out.println("\n❌ API调用失败！");
                }
            }
        } catch (Exception e) {
            System.out.println("请求异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}