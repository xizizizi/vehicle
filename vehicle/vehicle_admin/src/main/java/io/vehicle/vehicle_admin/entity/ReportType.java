package io.vehicle.vehicle_admin.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * 
 * @author xixixizi 
 * @version 2025/8/10 09:44
 * @since JDK22
 */

@TableName(value = "report_types", autoResultMap = true)
public class ReportType {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id; // 改为小写 id

    @TableField(value = "type_name")
    private String typeName;

    public String getTypeName() {
        return typeName;
    }
}

