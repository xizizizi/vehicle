package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.vehicle.vehicle_admin.handler.EnumTypeHandler;
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

    // Getter/Setter
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

    // MissionType 枚举类
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

    // MissionStatus 枚举类
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

    // 常用任务类型
    public static final String[] COMMON_MISSION_TYPES = {
            "交通巡查", "快递配送", "区域监控", "应急响应"
    };
}