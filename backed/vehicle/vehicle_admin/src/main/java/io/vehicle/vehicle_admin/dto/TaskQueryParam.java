package io.vehicle.vehicle_admin.dto;

import lombok.Data;

@Data
public class TaskQueryParam {
    private String systemType;
    private String severityLevel;
    private String status;
    private String taskName;
    private String droneId;
    private Integer areaId;
    private String startTime;
    private String endTime;
    private Integer page = 0;
    private Integer size = 20;
}