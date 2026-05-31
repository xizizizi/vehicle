package io.vehicle.vehicle_admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.Area;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AreaMapper extends BaseMapper<Area> {
    @Select("SELECT type, COUNT(*) as count, SUM(base_value) as total_value " +
            "FROM areas WHERE deleted = 0 GROUP BY type")
    List<Map<String, Object>> selectAreaStats();

    @Select("SELECT * FROM areas ORDER BY created_at DESC")
    List<Area> selectAllAreas();

    @Select("SELECT name, base_value, unit FROM areas WHERE type = #{type}")
    List<Map<String, Object>> selectAreasByType(String type);
}