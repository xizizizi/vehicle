package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value  = "task_types", autoResultMap = true)
public class TaskType {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "type_name")
    private String typeName;

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getId() {
        return id;
    }

    public String getTypeName() {
        return typeName;
    }
}