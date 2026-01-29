package io.vehicle.vehicle_admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.InspectionData;
import io.vehicle.vehicle_admin.entity.InspectionMedia;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface InspectionDataMapper extends BaseMapper<InspectionData> {

    @Insert("INSERT INTO inspection_data(task_name, area_id, drone_id, uploader_id, capture_time, latitude, longitude, altitude, file_count, data_type, description, status) " +
            "VALUES(#{taskName}, #{areaId}, #{droneId}, #{uploaderId}, #{captureTime}, #{latitude}, #{longitude}, #{altitude}, #{fileCount}, #{dataType}, #{description}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertData(InspectionData data);

    @Select("SELECT * FROM inspection_data WHERE id = #{id}")
    InspectionData selectById(@Param("id") Long id);

    @Select("<script>"
            + "SELECT * FROM inspection_data WHERE 1=1 "
            + "<if test='taskName != null'> AND task_name LIKE CONCAT('%',#{taskName},'%')</if>"
            + "<if test='droneId != null'> AND drone_id = #{droneId}</if>"
            + "<if test='areaId != null'> AND area_id = #{areaId}</if>"
            + "<if test='startTime != null'> AND capture_time &gt;= #{startTime}</if>"
            + "<if test='endTime != null'> AND capture_time &lt;= #{endTime}</if>"
            + "<if test='minLat != null and maxLat != null and minLng != null and maxLng != null'> AND latitude BETWEEN #{minLat} AND #{maxLat} AND longitude BETWEEN #{minLng} AND #{maxLng}</if>"
            + " ORDER BY capture_time DESC LIMIT #{offset}, #{limit}"
            + "</script>")
    List<InspectionData> queryList(Map<String, Object> params);

    @Select("SELECT COUNT(1) FROM inspection_data WHERE 1=1 "
            + "<if test='taskName != null'> AND task_name LIKE CONCAT('%',#{taskName},'%')</if>")
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

    // 在 InspectionDataMapper.java 中添加
    @Select("SELECT * FROM inspection_data WHERE status = #{status}")
    List<InspectionData> selectByStatus(String status);

    @Select("SELECT * FROM inspection_data WHERE created_at >= #{startDate}")
    List<InspectionData> selectByDateRange(LocalDateTime startDate);
}
