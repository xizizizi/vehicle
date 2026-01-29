package io.vehicle.vehicle_admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.vehicle.vehicle_admin.handler.EnumTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.vehicle.vehicle_admin.handler.OrderStatusTypeHandler;

import java.time.LocalDateTime;

@TableName(value = "orders", autoResultMap = true)

public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "order_sn")
    private String orderSn;

    @TableField(value = "status", typeHandler = OrderStatusTypeHandler.class)
    private OrderStatus status;

    @TableField(value = "pickup_lng")
    private Double pickupLng;

    @TableField(value = "pickup_lat")
    private Double pickupLat;

    @TableField(value = "delivery_lng")
    private Double deliveryLng;

    @TableField(value = "delivery_lat")
    private Double deliveryLat;

    private Double weight;

    @TableField(value = "assigned_uav_id")
    private Integer assignedUavId;

    @TableField(value = "created_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @TableField(value = "updated_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    @TableLogic
    private Integer deleted;

    // Getter/Setter
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getOrderSn() { return orderSn; }
    public void setOrderSn(String orderSn) { this.orderSn = orderSn; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Double getPickupLng() { return pickupLng; }
    public void setPickupLng(Double pickupLng) { this.pickupLng = pickupLng; }

    public Double getPickupLat() { return pickupLat; }
    public void setPickupLat(Double pickupLat) { this.pickupLat = pickupLat; }

    public Double getDeliveryLng() { return deliveryLng; }
    public void setDeliveryLng(Double deliveryLng) { this.deliveryLng = deliveryLng; }

    public Double getDeliveryLat() { return deliveryLat; }
    public void setDeliveryLat(Double deliveryLat) { this.deliveryLat = deliveryLat; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public Integer getAssignedUavId() { return assignedUavId; }
    public void setAssignedUavId(Integer assignedUavId) { this.assignedUavId = assignedUavId; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    // OrderStatus 枚举类
    public enum OrderStatus {
        PENDING("待处理"),
        ASSIGNED("已分配"),
        DELIVERING("配送中"),
        COMPLETED("已完成"),
        FAILED("失败");

        private final String chineseName;

        OrderStatus(String chineseName) {
            this.chineseName = chineseName;
        }

        public String getChineseName() {
            return chineseName;
        }
    }
}