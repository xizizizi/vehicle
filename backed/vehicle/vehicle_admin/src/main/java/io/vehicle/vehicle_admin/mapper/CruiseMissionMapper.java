package io.vehicle.vehicle_admin.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.CruiseMission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CruiseMissionMapper extends BaseMapper<CruiseMission> {

    @Select("SELECT * FROM cruise_mission WHERE status = 'ACTIVE' AND deleted = 0")
    List<CruiseMission> selectActiveMissions();
}
