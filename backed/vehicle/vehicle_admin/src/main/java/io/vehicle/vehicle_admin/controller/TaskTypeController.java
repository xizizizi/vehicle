package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.entity.TaskType;
import io.vehicle.vehicle_admin.service.TaskTypeService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task-types")
public class TaskTypeController {

    @Resource
    private TaskTypeService taskTypeService;

    @PostMapping
    public TaskType createTaskType(@RequestBody TaskType taskType) {
        return taskTypeService.saveTaskType(taskType);
    }

    @GetMapping("/by-name")
    public TaskType getTaskTypeByName(@RequestParam String name) {
        return taskTypeService.findTaskTypeByName(name);
    }


}