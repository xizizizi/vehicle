package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import io.vehicle.vehicle_admin.handler.MetaTypeHandler;

import java.util.List;

@TableName(value = "menu")
public class Menu {
    @TableId(value = "menu_id", type = IdType.AUTO)
    private Integer menuId;
    private String name;
    private String redirect ;
    private String path;
    private String component;
    @TableField(value = "parent_id")
    private Integer parentId;

    @TableField(typeHandler = MetaTypeHandler.class)
    private Meta meta;

    @TableField(exist = false)
    private List<Menu> children;
    public List<Menu> getChildren() {
        return children;
    }

    public void setChildren(List<Menu> children) {
        this.children = children;
    }

    public Integer getParentId() {
        return parentId;
    }




    public Menu() {
    }

    public Integer getMenuId() {
        return menuId;
    }

    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public Integer getParentIid() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "Menu{" +
                "menuId=" + menuId +
                ", name='" + name + '\'' +
                ", redirect='" + redirect + '\'' +
                ", path='" + path + '\'' +
                ", component='" + component + '\'' +
                ", parentIid=" + parentId +
                ", meta=" + meta +
                ", children=" + children +
                '}';
    }


}

