package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.vehicle.vehicle_admin.handler.EnumTypeHandler;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName(value = "inspection_reports", autoResultMap = true)
public class InspectionReports {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id; // 改为小写 id

    private String reportName;

    @TableField(value = "inspection_type", typeHandler = EnumTypeHandler.class)
    private InspectionType inspectionType; // 使用 ENUM 类型

    @TableField(value = "status", typeHandler = EnumTypeHandler.class)
    private Status status; // 使用 ENUM 类型

    @TableField(value = "inspection_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime inspectionDate;

    @TableField(value = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    // Getter/Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public InspectionType getInspectionType() { return inspectionType; }
    public void setInspectionType(InspectionType inspectionType) { this.inspectionType = inspectionType; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDateTime getInspectionDate() { return inspectionDate; }
    public void setInspectionDate(LocalDateTime inspectionDate) { this.inspectionDate = inspectionDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

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
