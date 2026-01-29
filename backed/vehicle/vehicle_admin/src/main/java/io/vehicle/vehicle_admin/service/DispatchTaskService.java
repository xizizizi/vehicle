package io.vehicle.vehicle_admin.service;

import io.vehicle.vehicle_admin.entity.DispatchTask;
import io.vehicle.vehicle_admin.entity.TaskCompletionDTO;
import io.vehicle.vehicle_admin.entity.TaskType;
import io.vehicle.vehicle_admin.mapper.DispatchTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class DispatchTaskService {

    @Autowired
    private DispatchTaskMapper dispatchTaskMapper;

    @Autowired
    private TaskTypeService taskTypeService;

    public DispatchTask createTask(String taskName, String taskTypeName, Date taskTime, Integer quantity) {
        TaskType taskType = taskTypeService.findTaskTypeByName(taskTypeName);
        if (taskType == null) {
            throw new RuntimeException("任务类型不存在: " + taskTypeName);
        }

        DispatchTask task = new DispatchTask();
        task.setTaskName(taskName);
        task.setTaskType(taskType);
        task.setTaskTime(taskTime);
        task.setQuantity(quantity);
        task.setStatus(DispatchTask.Status.PENDING);

        dispatchTaskMapper.insert(task);
        return task;
    }

    public List<DispatchTask> getTasksByTimeRange(Date startDate, Date endDate) {
        return dispatchTaskMapper.findByTaskTimeBetween(startDate, endDate);
    }

    public List<DispatchTask> getTasksByType(Integer taskTypeId) {
        return dispatchTaskMapper.findByTaskTypeId(taskTypeId);
    }

    public DispatchTask updateTaskStatus(Integer taskId, DispatchTask.Status status) {
        DispatchTask task = dispatchTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException(STR."任务未找到, ID: \{taskId}");
        }
        task.setStatus(status);
        dispatchTaskMapper.updateById(task);
        return task;
    }

    // DispatchTaskService.java

    public List<TaskCompletionDTO> getWeeklyCompletionStats() {
        // 获取本周的开始和结束日期（周一00:00到周日23:59）
//        LocalDate today = LocalDate.now();
//        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
//        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);
//
//        // 转换为Date对象
//        Date startDate = Date.from(startOfWeek.atStartOfDay(ZoneId.systemDefault()).toInstant());
//        Date endDate = Date.from(endOfWeek.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
//
//        // 查询本周完成的任务统计
//        return dispatchTaskMapper.getCompletionStats(startDate, endDate);


        return dispatchTaskMapper.getCompletionStats(null, null);
    }

    public List<DispatchTask> getLatestTasks() {
        return dispatchTaskMapper.findLatestTasks(10);
    }
}