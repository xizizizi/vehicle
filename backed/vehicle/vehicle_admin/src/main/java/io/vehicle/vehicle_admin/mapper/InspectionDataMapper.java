package io.vehicle.vehicle_admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.InspectionData;
import io.vehicle.vehicle_admin.entity.InspectionMedia;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface InspectionDataMapper extends BaseMapper<InspectionData> {

    // ✅ 修复：更新 INSERT 语句，包含所有新增字段
    @Insert("INSERT INTO inspection_data(" +
            "task_name, area_id, drone_id, uploader_id, capture_time, " +
            "latitude, longitude, altitude, file_count, data_type, " +
            "description, status, system_type, severity_level, " +
            "response_time, resolution_time, additional_data, area_type, " +
            "created_at, updated_at) " +
            "VALUES(" +
            "#{taskName}, #{areaId}, #{droneId}, #{uploaderId}, #{captureTime}, " +
            "#{latitude}, #{longitude}, #{altitude}, #{fileCount}, #{dataType}, " +
            "#{description}, #{status}, #{systemType}, #{severityLevel}, " +
            "#{responseTime}, #{resolutionTime}, #{additionalData}, #{areaType}, " +
            "#{createdAt}, #{updatedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertData(InspectionData data);

    @Select("SELECT * FROM inspection_data WHERE id = #{id}")
    InspectionData selectById(@Param("id") Long id);

    @Select("<script>"
            + "SELECT * FROM inspection_data WHERE 1=1 "
            + "<if test='taskName != null and taskName != \"\"'> AND task_name LIKE CONCAT('%',#{taskName},'%')</if>"
            + "<if test='droneId != null and droneId != \"\"'> AND drone_id = #{droneId}</if>"
            + "<if test='areaId != null'> AND area_id = #{areaId}</if>"
            + "<if test='startTime != null'> AND capture_time &gt;= #{startTime}</if>"
            + "<if test='endTime != null'> AND capture_time &lt;= #{endTime}</if>"
            + "<if test='minLat != null'> AND latitude &gt;= #{minLat}</if>"
            + "<if test='maxLat != null'> AND latitude &lt;= #{maxLat}</if>"
            + "<if test='minLng != null'> AND longitude &gt;= #{minLng}</if>"
            + "<if test='maxLng != null'> AND longitude &lt;= #{maxLng}</if>"
            // 新增系统类型过滤
            + "<if test='systemType != null and systemType != \"\"'> AND system_type = #{systemType}</if>"
            // 新增严重程度过滤
            + "<if test='severityLevel != null and severityLevel != \"\"'> AND severity_level = #{severityLevel}</if>"
            + " ORDER BY id DESC"
            + "<if test='limit != null'> LIMIT #{limit}</if>"
            + "<if test='offset != null'> OFFSET #{offset}</if>"
            + "</script>")
    List<InspectionData> queryList(Map<String, Object> params);

    @Select("<script>"
            + "SELECT COUNT(1) FROM inspection_data WHERE 1=1 "
            + "<if test='taskName != null and taskName != \"\"'> AND task_name LIKE CONCAT('%',#{taskName},'%')</if>"
            + "<if test='droneId != null and droneId != \"\"'> AND drone_id = #{droneId}</if>"
            + "<if test='areaId != null'> AND area_id = #{areaId}</if>"
            + "<if test='startTime != null'> AND capture_time &gt;= #{startTime}</if>"
            + "<if test='endTime != null'> AND capture_time &lt;= #{endTime}</if>"
            + "<if test='minLat != null'> AND latitude &gt;= #{minLat}</if>"
            + "<if test='maxLat != null'> AND latitude &lt;= #{maxLat}</if>"
            + "<if test='minLng != null'> AND longitude &gt;= #{minLng}</if>"
            + "<if test='maxLng != null'> AND longitude &lt;= #{maxLng}</if>"
            + "<if test='systemType != null and systemType != \"\"'> AND system_type = #{systemType}</if>"
            + "<if test='severityLevel != null and severityLevel != \"\"'> AND severity_level = #{severityLevel}</if>"
            + "</script>")
    int countByFilter(Map<String, Object> params);

    // InspectionMedia 操作
    @Insert("INSERT INTO inspection_media(data_id, file_name, storage_path, mime_type, file_size, thumb_path, capture_time, latitude, longitude, altitude, exif) " +
            "VALUES(#{dataId}, #{fileName}, #{storagePath}, #{mimeType}, #{fileSize}, #{thumbPath}, #{captureTime}, #{latitude}, #{longitude}, #{altitude}, #{exif})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertMedia(InspectionMedia media);

    @Select("SELECT * FROM inspection_media WHERE data_id = #{dataId}")
    List<InspectionMedia> selectMediaByDataId(@Param("dataId") Long dataId);

    @Update("UPDATE inspection_data SET file_count = #{count} WHERE id = #{id}")
    int updateFileCount(@Param("id") Long id, @Param("count") Integer count);

    @Select("SELECT * FROM inspection_data WHERE status = #{status}")
    List<InspectionData> selectByStatus(String status);

    @Select("SELECT * FROM inspection_data WHERE created_at >= #{startDate}")
    List<InspectionData> selectByDateRange(LocalDateTime startDate);
}