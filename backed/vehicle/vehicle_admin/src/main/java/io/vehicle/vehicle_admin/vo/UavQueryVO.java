package io.vehicle.vehicle_admin.vo;

import io.vehicle.vehicle_admin.entity.Uav;
import lombok.Data;

@Data
public class UavQueryVO {
    private String sn;

    private String model;

    private Uav.UavStatus status;

    private Integer minBattery;

    private Integer maxBattery;

    private Integer pageNum = 1;

    private Integer pageSize = 20;
}