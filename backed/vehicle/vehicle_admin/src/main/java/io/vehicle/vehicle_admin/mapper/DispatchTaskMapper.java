package io.vehicle.vehicle_admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.DispatchTask;
import io.vehicle.vehicle_admin.entity.TaskCompletionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

@Mapper
public interface DispatchTaskMapper extends BaseMapper<DispatchTask> {

    @Select("SELECT * FROM dispatch_tasks WHERE task_time BETWEEN #{startDate} AND #{endDate}")
    List<DispatchTask> findByTaskTimeBetween(@Param("startDate") Date startDate,
                                             @Param("endDate") Date endDate);

    @Select("SELECT * FROM dispatch_tasks WHERE task_type_id = #{taskTypeId}")
    List<DispatchTask> findByTaskTypeId(@Param("taskTypeId") Integer taskTypeId);


//    // DispatchTaskMapper.java
//
//    @Select("SELECT tt.type_name AS type, COUNT(dt.id) AS count " +
//            "FROM dispatch_tasks dt " +
//            "JOIN task_types tt ON dt.task_type_id = tt.id " +
//            "WHERE dt.status = 'COMPLETED' " +
//            "AND dt.task_time BETWEEN #{start} AND #{end} " +
//            "GROUP BY tt.type_name")
//    List<TaskCompletionDTO> getCompletionStats(@Param("start") Date start,
//                                               @Param("end") Date end);
    @Select("<script>" +
            "SELECT tt.type_name AS type, COUNT(dt.id) AS count " +
            "FROM dispatch_tasks dt " +
            "JOIN task_types tt ON dt.task_type_id = tt.id " +
            "WHERE dt.status = 'COMPLETED' " +
            "<if test='start != null and end != null'>" +
            "AND dt.task_time BETWEEN #{start} AND #{end} " +
            "</if>" +
            "GROUP BY tt.type_name" +
            "</script>")
    List<TaskCompletionDTO> getCompletionStats(@Param("start") Date start,
                                               @Param("end") Date end);



    @Select("SELECT dt.* " +  // 只查询任务表字段
            "FROM dispatch_tasks dt " +
            "ORDER BY dt.task_time DESC LIMIT #{count}")
    List<DispatchTask> findLatestTasks(@Param("count") int count);
}