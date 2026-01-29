package io.vehicle.vehicle_admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.TaskType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskTypeMapper extends BaseMapper<TaskType> {

//    @Select("SELECT * FROM task_types WHERE type_name = #{typeName}")
//    TaskType findByTypeName(@Param("typeName") String typeName);

    @Select("SELECT * FROM task_types WHERE type_name = #{typeName} COLLATE utf8mb4_bin")
    TaskType findByTypeName(String typeName);
}