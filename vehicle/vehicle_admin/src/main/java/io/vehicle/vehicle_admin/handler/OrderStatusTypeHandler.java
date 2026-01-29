package io.vehicle.vehicle_admin.handler;

import io.vehicle.vehicle_admin.entity.Order;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(Order.OrderStatus.class)
public class OrderStatusTypeHandler extends EnumTypeHandler<Order.OrderStatus> {
    public OrderStatusTypeHandler() {
        super(Order.OrderStatus.class);
    }
}