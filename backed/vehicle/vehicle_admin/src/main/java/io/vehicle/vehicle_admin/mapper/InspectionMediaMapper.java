package io.vehicle.vehicle_admin.mapper;

import io.vehicle.vehicle_admin.entity.InspectionMedia;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InspectionMediaMapper {

    @Select("SELECT * FROM inspection_media WHERE data_id = #{dataId} ORDER BY id")
    List<InspectionMedia> findByDataId(@Param("dataId") Long dataId);

    @Insert("INSERT INTO inspection_media(data_id, file_name, storage_path, mime_type, file_size, thumb_path, capture_time, latitude, longitude, altitude, exif, created_at) " +
            "VALUES(#{dataId}, #{fileName}, #{storagePath}, #{mimeType}, #{fileSize}, #{thumbPath}, #{captureTime}, #{latitude}, #{longitude}, #{altitude}, #{exif}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(InspectionMedia media);

    @Select("SELECT * FROM inspection_media WHERE id = #{id}")
    InspectionMedia selectById(@Param("id") Long id);

}
