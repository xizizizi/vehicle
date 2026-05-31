package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.vehicle.vehicle_admin.handler.EnumTypeHandler;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName(value = "inspection_reports", autoResultMap = true)
public class InspectionReports {

    // Getter/Setter
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id; // 改为小写 id

    private String reportName;

    @TableField(value = "inspection_type", typeHandler = EnumTypeHandler.class)
    private InspectionType inspectionType; // 使用 ENUM 类型

    @TableField(value = "quantity")
    private Integer quantity; // 上报数量

    @TableField(value = "report_type_id")
    private Integer reportTypeId; // 上报类型

    @TableField(value = "area_id")
    private Integer areaId; // 关联区域

    @TableField(value = "status", typeHandler = EnumTypeHandler.class)
    private Status status; // 使用 ENUM 类型

    @TableField(value = "report_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inspectionDate;

    @TableField(value = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @TableField(value = "details")
    private String details; // 详情描述

    // InspectionType 枚举类
    public enum InspectionType {
        ROUTINE("常规检查"),
        SPECIAL("特殊检查"),
        EMERGENCY("紧急检查"),
        REPAIR("维修检查");

        private final String chineseName;

        InspectionType(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }

    // Status 枚举类
    public enum Status {
        PENDING("待处理"),
        COMPLETED("已完成"),
        IN_PROGRESS("进行中"),
        CANCELED("已取消");

        private final String chineseName;

        Status(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }
}
