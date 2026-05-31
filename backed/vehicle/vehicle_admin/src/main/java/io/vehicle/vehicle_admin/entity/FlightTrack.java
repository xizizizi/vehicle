package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@TableName(value = "flight_track", autoResultMap = true)
public class FlightTrack {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "uav_id")
    private Integer uavId;

    @TableField(value = "mission_id")
    private Integer missionId;

    private Double lng;

    private Double lat;

    private Double altitude;

    private Double speed;

    private Integer battery;

    @TableField(value = "recorded_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordedTime;

    // Getter/Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getUavId() { return uavId; }
    public void setUavId(Integer uavId) { this.uavId = uavId; }

    public Integer getMissionId() { return missionId; }
    public void setMissionId(Integer missionId) { this.missionId = missionId; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getAltitude() { return altitude; }
    public void setAltitude(Double altitude) { this.altitude = altitude; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Integer getBattery() { return battery; }
    public void setBattery(Integer battery) { this.battery = battery; }

    public LocalDateTime getRecordedTime() { return recordedTime; }
    public void setRecordedTime(LocalDateTime recordedTime) { this.recordedTime = recordedTime; }
}