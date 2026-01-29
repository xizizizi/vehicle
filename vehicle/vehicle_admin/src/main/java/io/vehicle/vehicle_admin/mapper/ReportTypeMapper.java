package io.vehicle.vehicle_admin.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.Area;
import io.vehicle.vehicle_admin.entity.ReportType;
import io.vehicle.vehicle_admin.handler.EnumTypeHandler;
import io.vehicle.vehicle_admin.service.ReportService;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ReportTypeMapper extends BaseMapper<ReportType> {
    @Select("SELECT * FROM report_types")
    List<ReportType> findAll();


    @Select("SELECT * FROM area_stats")
    @Results({
            @Result(column = "area_type", property = "areaType",
                    javaType = Area.AreaType.class, typeHandler = EnumTypeHandler.class),
            @Result(column = "total_area", property = "totalArea"),
            @Result(column = "report_count", property = "reportCount")
    })
    List<ReportService.AreaStat> getAreaStats();
}

