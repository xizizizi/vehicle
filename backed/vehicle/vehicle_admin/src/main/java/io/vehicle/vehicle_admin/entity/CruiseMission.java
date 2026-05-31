package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.vehicle.vehicle_admin.handler.MissionStatusTypeHandler;
import io.vehicle.vehicle_admin.handler.MissionTypeTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@TableName(value = "cruise_mission", autoResultMap = true)
public class CruiseMission {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "mission_name")
    private String missionName;

    @TableField(value = "mission_type", typeHandler = MissionTypeTypeHandler.class)
    private MissionType missionType;

    @TableField(value = "status", typeHandler = MissionStatusTypeHandler.class)
    private MissionStatus status;

    @TableField(value = "route_points")
    private String routePoints;

    @TableField(value = "assigned_uav_id")
    private Integer assignedUavId;

    private Integer priority;

    @TableField(value = "start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @TableField(value = "end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    // === 新增字段开始 ===
    @TableField(value = "system_type")
    private String systemType; // 系统类型: LOGISTICS(物流), MEDICAL(医疗)

    @TableField(value = "severity_level")
    private String severityLevel; // 严重程度: CRITICAL, HIGH, MEDIUM, NORMAL

    @TableField(value = "hospital_name")
    private String hospitalName; // 医院名称

    @TableField(value = "response_time")
    private Integer responseTime; // 响应时间要求(分钟)

    @TableField(value = "medical_data_id")
    private Long medicalDataId; // 关联的巡检数据ID
    // === 新增字段结束 ===

    // MissionType 枚举类（保持不变）
    public enum MissionType {
        CRUISE("巡航"),
        EXPRESS("快递"),
        SURVEILLANCE("监控");

        private final String chineseName;

        MissionType(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }

    // MissionStatus 枚举类（保持不变）
    public enum MissionStatus {
        PENDING("待分配"),
        ACTIVE("进行中"),
        PAUSED("已暂停"),
        COMPLETED("已完成"),
        CANCELLED("已取消");

        private final String chineseName;

        MissionStatus(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }

    // 常用任务类型（保持不变）
    public static final String[] COMMON_MISSION_TYPES = {
            "交通巡查", "快递配送", "区域监控", "应急响应"
    };
}