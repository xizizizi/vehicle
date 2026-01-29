package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@TableName(value = "inspection_data", autoResultMap = true)
public class InspectionData {

    // ========== GETTERS / SETTERS ==========
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("task_name")
    private String taskName;

    @TableField("area_id")
    private Integer areaId;

    @TableField("drone_id")
    private String droneId;

    @TableField("uploader_id")
    private Integer uploaderId;

    @TableField("capture_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime captureTime;

    private Double latitude;
    private Double longitude;
    private Double altitude;

    @TableField("file_count")
    private Integer fileCount;

    /**
     * 不使用外部 EnumTypeHandler，使用 @EnumValue 让 MyBatis-Plus 写入枚举的 code 值（如 "PHOTO"）
     * 若读取时 MyBatis 无法自动映射回枚举，可用 DataType.fromCode(...) 做手动转换
     */
    @TableField("data_type")
    private DataType dataType;

    private String description;

    @TableField("status")
    private Status status;

    @TableField("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ========== ENUMS ==========
    public enum DataType {
        PHOTO("PHOTO", "照片"),
        VIDEO("VIDEO", "视频"),
        ORTHO("ORTHO", "正射影像"),
        MODEL("MODEL", "三维模型"),
        POINTCLOUD("POINTCLOUD", "点云"),
        OTHER("OTHER", "其他");

        @EnumValue
        private final String code;
        private final String label;

        DataType(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public String getLabel() { return label; }

        public static DataType fromCode(String code) {
            if (code == null) return null;
            for (DataType t : values()) {
                if (t.code.equalsIgnoreCase(code)) return t;
            }
            return null;
        }

        @JsonCreator
        public static DataType jsonCreate(String code) {
            return fromCode(code);
        }
    }

    public enum Status {
        PENDING("PENDING", "待审核"),
        APPROVED("APPROVED", "已审核"),
        ARCHIVED("ARCHIVED", "已归档"),
        DELETED("DELETED", "已删除");

        @EnumValue
        private final String code;
        private final String label;

        Status(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public String getLabel() { return label; }

        public static Status fromCode(String code) {
            if (code == null) return null;
            for (Status s : values()) {
                if (s.code.equalsIgnoreCase(code)) return s;
            }
            return null;
        }

        @JsonCreator
        public static Status jsonCreate(String code) {
            return fromCode(code);
        }
    }
    // 在 InspectionData 实体类中添加辅助方法
    public static class StatusHelper {
        public static boolean isCompleted(Object status) {
            if (status == null) return false;
            String statusStr = status.toString().toUpperCase();
            return statusStr.contains("COMPLETED") ||
                    statusStr.contains("DONE") ||
                    statusStr.contains("FINISHED") ||
                    statusStr.contains("已完成");
        }

        public static boolean isActive(Object status) {
            if (status == null) return false;
            String statusStr = status.toString().toUpperCase();
            return statusStr.contains("ACTIVE") ||
                    statusStr.contains("IN_PROGRESS") ||
                    statusStr.contains("PROCESSING") ||
                    statusStr.contains("EXECUTING") ||
                    statusStr.contains("进行中");
        }

        public static boolean isPending(Object status) {
            if (status == null) return false;
            String statusStr = status.toString().toUpperCase();
            return statusStr.contains("PENDING") ||
                    statusStr.contains("WAITING") ||
                    statusStr.contains("待处理");
        }
    }
}
