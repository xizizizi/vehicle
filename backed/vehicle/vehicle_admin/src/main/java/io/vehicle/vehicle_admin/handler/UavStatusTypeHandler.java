package io.vehicle.vehicle_admin.handler;

import io.vehicle.vehicle_admin.entity.Uav;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(Uav.UavStatus.class)
public class UavStatusTypeHandler extends EnumTypeHandler<Uav.UavStatus> {
    public UavStatusTypeHandler() {
        super(Uav.UavStatus.class);
    }
}