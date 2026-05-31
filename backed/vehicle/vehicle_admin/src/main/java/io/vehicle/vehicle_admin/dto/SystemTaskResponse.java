package io.vehicle.vehicle_admin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class SystemTaskResponse {
    private Long id;
    private String taskName;
    private String systemType; // MEDICAL, POWER, FOREST, INSPECTION
    private String severityLevel; // CRITICAL, HIGH, MEDIUM, NORMAL
    private String status;
    private String description;
    private Integer responseTime; // 分钟
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, Object> additionalData;
    private Integer fileCount;
}