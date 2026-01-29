package io.vehicle.vehicle_admin.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "role")
public class Role {
    @TableId(value = "role_id",type = IdType.AUTO)
    private Integer roleId;
    @TableField(value = "role_name")
    private String roleName;


    public Role(){

    }
    public Role(Integer roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;

    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return "Role{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                '}';
    }
}

