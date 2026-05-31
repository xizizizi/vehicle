package io.vehicle.vehicle_admin.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class CozeChatRequest {
    private String bot_id;
    private String user_id;
    private Boolean stream = false;
    private List<Message> additional_messages;
    private Map<String, Object> parameters;

    @Data
    public static class Message {
        private String content;
        private String content_type = "text";
        private String role = "user";
        private String type = "question";
    }
}