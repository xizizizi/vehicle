package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.entity.DispatchTask;
import io.vehicle.vehicle_admin.entity.TaskCompletionDTO;
import io.vehicle.vehicle_admin.service.DispatchTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/tasks")
public class DispatchTaskController {
    @Autowired
    private DispatchTaskService dispatchTaskService;

    // 请求参数类
    public static class DispatchTaskRequest {
        private String taskName;
        private String taskTypeName;
        private Date taskTime;
        private Integer quantity;

        // Getters and Setters
        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }

        public String getTaskTypeName() {
            return taskTypeName;
        }

        public void setTaskTypeName(String taskTypeName) {
            this.taskTypeName = taskTypeName;
        }

        public Date getTaskTime() {
            return taskTime;
        }

        public void setTaskTime(Date taskTime) {
            this.taskTime = taskTime;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    @PostMapping
    public DispatchTask createTask(@RequestBody DispatchTaskRequest request) {
        return dispatchTaskService.createTask(
                request.getTaskName(),
                request.getTaskTypeName(),
                request.getTaskTime(),
                request.getQuantity()
        );
    }

    // 修改 Controller 方法
    @GetMapping("/by-time")
    public List<DispatchTask> getTasksByTimeRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        // 转换为 LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);

        return dispatchTaskService.getTasksByTimeRange(
                Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant()),
                Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant())
        );
    }
    @GetMapping("/by-type")
    public List<DispatchTask> getTasksByType(@RequestParam Integer taskTypeId) {
        return dispatchTaskService.getTasksByType(taskTypeId);
    }

    @PutMapping("/{id}/status")
    public DispatchTask updateTaskStatus(@PathVariable Integer id, @RequestParam DispatchTask.Status status) {
        return dispatchTaskService.updateTaskStatus(id, status);
    }

    @GetMapping("/weekly-completion")
    public List<TaskCompletionDTO> getWeeklyCompletion() {
        return dispatchTaskService.getWeeklyCompletionStats();
    }


    @GetMapping("/latest")
    public List<DispatchTask> getLatestTasks() {
        return dispatchTaskService.getLatestTasks();
    }
}