package io.vehicle.vehicle_admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.Uav;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface UavMapper extends BaseMapper<Uav> {

    @Select("SELECT * FROM uav WHERE status = 'IDLE' AND battery_level > #{minBattery} AND deleted = 0")
    List<Uav> selectIdleUavsWithGoodBattery(@Param("minBattery") Integer minBattery);

    //12/7
    // 根据SN查询无人机是否存在
    @Select("SELECT COUNT(*) FROM uav WHERE sn = #{sn} AND deleted = 0")
    int countBySn(@Param("sn") String sn);

    // 获取无人机状态统计
    @Select("SELECT status, COUNT(*) as count FROM uav WHERE deleted = 0 GROUP BY status")
    List<Map<String, Object>> getStatusStatistics();

    // 根据经纬度范围查询无人机
    @Select("SELECT * FROM uav WHERE deleted = 0 AND current_lat BETWEEN #{minLat} AND #{maxLat} " +
            "AND current_lng BETWEEN #{minLng} AND #{maxLng}")
    List<Uav> findByLocationRange(@Param("minLat") Double minLat,
                                  @Param("maxLat") Double maxLat,
                                  @Param("minLng") Double minLng,
                                  @Param("maxLng") Double maxLng);
}