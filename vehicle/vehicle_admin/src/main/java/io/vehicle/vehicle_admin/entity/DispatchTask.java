package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.vehicle.vehicle_admin.handler.EnumTypeHandler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@TableName(value = "dispatch_tasks", autoResultMap = true)
public class DispatchTask {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "task_name")
    private String taskName;

    @TableField(value = "task_type_id")
    private Integer taskTypeId;

    @TableField(value = "task_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime taskTime;

    @TableField(value = "quantity")
    private Integer quantity;

    @TableField(value = "status", typeHandler = EnumTypeHandler.class)
    private Status status;

    // 添加非持久化字段用于业务处理
    @TableField(exist = false)
    private TaskType taskType;
    @TableField(exist = false) // 标记为非数据库字段
    private String taskTypeName; // 新增字段

    // Getter/Setter
    public String getTaskTypeName() {
        return taskTypeName;
    }
    public void setTaskTypeName(String taskTypeName) {
        this.taskTypeName = taskTypeName;
    }
    public enum Status {
        COMPLETED("已完成"),
        EXECUTING("执行中"),
        PENDING("待处理");

        // 保持中文描述，但数据库存储英文值
        private final String chineseName;

        Status(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }

    public void setTaskTime(LocalDateTime taskTime) {
        this.taskTime = taskTime;
    }

    public void setTaskTime(Date taskTime) {
        this.taskTime = taskTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
        if (taskType != null) {
            this.taskTypeId = taskType.getId();
        }
    }

    // Getter 和 Setter
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getTaskTypeId() {
        return taskTypeId;
    }

    public void setTaskTypeId(Integer taskTypeId) {
        this.taskTypeId = taskTypeId;
    }

    public LocalDateTime getTaskTime() {
        return taskTime;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public TaskType getTaskType() {
        return taskType;
    }
}