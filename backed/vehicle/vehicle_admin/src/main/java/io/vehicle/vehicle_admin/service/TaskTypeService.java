package io.vehicle.vehicle_admin.service;

import io.vehicle.vehicle_admin.entity.TaskType;
import io.vehicle.vehicle_admin.mapper.TaskTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskTypeService {

    @Autowired
    private TaskTypeMapper taskTypeMapper;

    public TaskType findTaskTypeByName(String typeName) {
        return taskTypeMapper.findByTypeName(typeName);
    }

    public TaskType saveTaskType(TaskType taskType) {
        // 添加唯一性检查
        if (taskTypeMapper.findByTypeName(taskType.getTypeName()) != null) {
            throw new RuntimeException("任务类型已存在: " + taskType.getTypeName());
        }
        taskTypeMapper.insert(taskType);
        return taskType;
    }
}