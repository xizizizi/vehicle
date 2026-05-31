// ReportMapper.java
package io.vehicle.vehicle_admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.Area;
import io.vehicle.vehicle_admin.entity.InspectionReports;
import io.vehicle.vehicle_admin.entity.ReportType;
import io.vehicle.vehicle_admin.handler.EnumTypeHandler;
import io.vehicle.vehicle_admin.service.ReportService;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ReportMapper extends BaseMapper<InspectionReports> {

    @Select("SELECT COUNT(*) FROM inspection_reports WHERE report_type_id = #{reportType.id}")
    int countByReportType(@Param("reportType") ReportType reportType);

    @Select("SELECT " +
            "a.type AS areaType, " +
            "SUM(CASE a.unit " +
            "    WHEN 'SQUARE_KM' THEN a.base_value * 100 " +
            "    WHEN 'HECTARE' THEN a.base_value " +
            "    ELSE 0 " +
            "END) AS totalArea, " +
            "COUNT(r.id) AS reportCount " +
            "FROM areas a " +
            "LEFT JOIN inspection_reports r ON a.id = r.area_id " +
            "WHERE a.type IN ('COMMERCIAL', 'RESIDENTIAL', 'INDUSTRIAL', 'NATURAL', 'SPECIAL') " +
            "GROUP BY a.type")
    @Results({
            @Result(column = "areaType", property = "areaType",
                    javaType = Area.AreaType.class, typeHandler = EnumTypeHandler.class),
            @Result(column = "totalArea", property = "totalArea"),
            @Result(column = "reportCount", property = "reportCount")
    })
    List<ReportService.AreaStat> getAreaStats();

    @Select("SELECT task_time, task_name, quantity, status " +
            "FROM dispatch_tasks ORDER BY task_time DESC LIMIT 10")
    List<Map<String, Object>> getLatestTasks();

    @Select("SELECT t.type_name AS name, COUNT(d.id) AS value " +
            "FROM dispatch_tasks d " +
            "JOIN task_types t ON d.task_type_id = t.id " +
            "WHERE d.status = '已完成' GROUP BY t.id")
    List<Map<String, Object>> getRoseTasks();

    @Select("SELECT rt.type_name AS name, COUNT(ir.id) AS value " +
            "FROM report_types rt " +
            "LEFT JOIN inspection_reports ir ON rt.id = ir.report_type_id " +
            "GROUP BY rt.id ORDER BY value DESC LIMIT 10")
    List<Map<String, Object>> getRanking();

}
