package io.vehicle.vehicle_admin.handler;

import io.vehicle.vehicle_admin.entity.CruiseMission;
import org.apache.ibatis.type.MappedTypes;

@MappedTypes(CruiseMission.MissionStatus.class)
public class MissionStatusTypeHandler extends EnumTypeHandler<CruiseMission.MissionStatus> {
    public MissionStatusTypeHandler() {
        super(CruiseMission.MissionStatus.class);
    }
}