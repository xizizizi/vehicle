package io.vehicle.vehicle_admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.FlightTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FlightTrackMapper extends BaseMapper<FlightTrack> {

    @Select("SELECT * FROM flight_track WHERE uav_id = #{uavId} ORDER BY recorded_time DESC LIMIT #{limit}")
    List<FlightTrack> selectRecentTracks(@Param("uavId") Integer uavId, @Param("limit") Integer limit);

    @Select("SELECT * FROM flight_track WHERE mission_id = #{missionId} ORDER BY recorded_time")
    List<FlightTrack> selectMissionTracks(@Param("missionId") Integer missionId);
}