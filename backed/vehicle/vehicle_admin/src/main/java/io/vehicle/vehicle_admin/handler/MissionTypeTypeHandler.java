package io.vehicle.vehicle_admin.handler;

import io.vehicle.vehicle_admin.entity.CruiseMission;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(CruiseMission.MissionType.class)
public class MissionTypeTypeHandler extends EnumTypeHandler<CruiseMission.MissionType> {
    public MissionTypeTypeHandler() {
        super(CruiseMission.MissionType.class);
    }
}