package io.vehicle.vehicle_admin.dto;

import lombok.Data;
import java.util.List;

@Data
public class CozeChatResponse {
    private Integer code;
    private String msg;
    private DataDTO data;

    @Data
    public static class DataDTO {
        private String id;
        private String conversation_id;
        private List<MessageDTO> messages;
        private String status;
    }

    @Data
    public static class MessageDTO {
        private String role;
        private String type;
        private String content;
    }
}