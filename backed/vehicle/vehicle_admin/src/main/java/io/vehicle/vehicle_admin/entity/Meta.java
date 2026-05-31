package io.vehicle.vehicle_admin.entity;


import java.util.List;

public class Meta {

    private String title;
    private String icon;
    private List<String> roles;
    private  Boolean affix;

    public Meta() {
    }
    public Meta(String title, String icon, List<String> roles, Boolean affix) {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Boolean getAffix() {
        return affix;
    }

    public void setAffix(Boolean affix) {
        this.affix = affix;
    }

    @Override
    public String toString() {
        return "Meta{" +
                "title='" + title + '\'' +
                ", icon='" + icon + '\'' +
                ", roles=" + roles +
                ", affix=" + affix +
                '}';
    }
}

