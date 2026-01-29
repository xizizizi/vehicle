package io.vehicle.vehicle_admin.vo;

import io.vehicle.vehicle_admin.entity.Uav;
import lombok.Data;

@Data
public class UavAddVO {

    private String sn;

    private String model;

    private Uav.UavStatus status;

    private Integer batteryLevel;

    // 地点名称（可选，与经纬度二选一）
    private String locationName;

    // 经度（可选）
    private Double currentLng;

    // 纬度（可选）
    private Double currentLat;

    private Double loadCapacity;

    private String currentMission;

    /**
     * 验证方法，返回错误消息，如果为null表示验证通过
     */
    public String validate() {
        if (sn == null || sn.trim().isEmpty()) {
            return "序列号不能为空";
        }
        if (!sn.matches("^[A-Za-z0-9_-]{6,32}$")) {
            return "序列号格式不正确，支持字母、数字、下划线，长度6-32位";
        }
        if (model == null || model.trim().isEmpty()) {
            return "型号不能为空";
        }
        if (batteryLevel != null && (batteryLevel < 0 || batteryLevel > 100)) {
            return "电量必须在0-100之间";
        }
        if (loadCapacity != null && (loadCapacity < 0.1 || loadCapacity > 100)) {
            return "载重必须在0.1-100kg之间";
        }

        // 位置验证
        boolean hasLocationName = locationName != null && !locationName.trim().isEmpty();
        boolean hasCoordinates = currentLng != null && currentLat != null;

        if (!hasLocationName && !hasCoordinates) {
            return "必须提供位置信息（地点名称或经纬度）";
        }

        if (hasCoordinates) {
            if (currentLng < 115.7 || currentLng > 116.1) {
                return "经度应在115.7-116.1之间（南昌范围）";
            }
            if (currentLat < 28.5 || currentLat > 28.9) {
                return "纬度应在28.5-28.9之间（南昌范围）";
            }
        }

        return null; // 验证通过
    }
}