package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.vehicle.vehicle_admin.handler.EnumTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.vehicle.vehicle_admin.handler.UavStatusTypeHandler;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@TableName(value = "uav", autoResultMap = true)
public class Uav {

    // Getter/Setter
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String sn;

    private String model;

    @TableField(value = "status", typeHandler = UavStatusTypeHandler.class)
    private UavStatus status;

    @TableField(value = "battery_level")
    private Integer batteryLevel;

    @TableField(value = "current_lng")
    private Double currentLng;

    @TableField(value = "current_lat")
    private Double currentLat;

    @TableField(value = "load_capacity")
    private Double loadCapacity;

    @TableField(value = "current_mission")
    private String currentMission;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    // UavStatus 枚举类
    public enum UavStatus {
        IDLE("空闲"),
        CHARGING("充电中"),
        ON_MISSION("任务中"),
        MAINTENANCE("维护中"),
        ERROR("故障");

        private final String chineseName;

        UavStatus(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }

    // 常用无人机型号
    public static final String[] COMMON_MODELS = {
            "DJI_Mavic_3", "DJI_Matrice_350", "DJI_Phantom_4", "DJI_Inspire_2"
    };
}