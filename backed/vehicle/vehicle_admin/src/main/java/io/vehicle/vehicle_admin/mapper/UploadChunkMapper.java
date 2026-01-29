package io.vehicle.vehicle_admin.mapper;

import io.vehicle.vehicle_admin.entity.UploadChunk;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UploadChunkMapper {

    // 修改 INSERT 语句以匹配数据库表结构
    @Insert("INSERT INTO upload_chunks(upload_id, chunk_index, total_chunks, chunk_path, uploaded_at) " +
            "VALUES(#{fileMd5}, #{chunkNumber}, #{totalChunks}, #{chunkPath}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertChunk(UploadChunk chunk);

    // 修改 SELECT 语句以匹配数据库表结构
    @Select("SELECT * FROM upload_chunks WHERE upload_id = #{fileMd5} ORDER BY chunk_index")
    List<UploadChunk> findByFileMd5(@Param("fileMd5") String fileMd5);

    // 修改 DELETE 语句以匹配数据库表结构
    @Delete("DELETE FROM upload_chunks WHERE upload_id = #{fileMd5}")
    void deleteByFileMd5(@Param("fileMd5") String fileMd5);
}