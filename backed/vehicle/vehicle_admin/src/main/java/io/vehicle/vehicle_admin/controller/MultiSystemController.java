package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.entity.InspectionData;
import io.vehicle.vehicle_admin.service.InspectionDataService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/multi-system")
@CrossOrigin
public class MultiSystemController {

    @Resource
    private InspectionDataService inspectionDataService;
    // 获取系统统计信息 - 简洁版（只返回各类任务数量）
    @GetMapping("/stats/simple")
    public ResponseEntity<?> getSystemStatsSimple() {
        try {
            // 获取所有任务
            Map<String, Object> allParams = new HashMap<>();
            List<InspectionData> allTasks = inspectionDataService.queryList(allParams);

            // 按系统类型统计
            Map<String, Long> systemTypeCount = allTasks.stream()
                    .filter(task -> task.getSystemType() != null)
                    .collect(Collectors.groupingBy(
                            InspectionData::getSystemType,
                            Collectors.counting()
                    ));

            // 计算三类任务的数量
            long medicalCount = systemTypeCount.getOrDefault("MEDICAL", 0L);
            long powerCount = systemTypeCount.getOrDefault("POWER", 0L);
            long forestCount = systemTypeCount.getOrDefault("FOREST", 0L);
            long otherCount = allTasks.size() - medicalCount - powerCount - forestCount;

            Map<String, Object> stats = new HashMap<>();
            stats.put("medical", medicalCount);
            stats.put("power", powerCount);
            stats.put("forest", forestCount);
            stats.put("other", otherCount);
            stats.put("total", allTasks.size());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", 200,
                    "data", stats
            ));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", 500,
                    "error", ex.getMessage()
            ));
        }
    }
    // 上传多系统任务 - 修复：确保能保存到数据库
    @PostMapping("/upload-task")
    public ResponseEntity<?> uploadMultiSystemTask(
            @RequestParam(required = false) String taskName,
            @RequestParam(required = false) String systemType, // MEDICAL, POWER, FOREST, INSPECTION
            @RequestParam(required = false) Integer areaId,
            @RequestParam(required = false) String droneId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String severityLevel,
            @RequestParam(required = false) Integer responseTime,
            @RequestParam(required = false) String additionalData,
            @RequestParam(required = false) MultipartFile[] files) {

        try {
            System.out.println("📱 多系统任务上传 - 开始处理");

            // 验证必填字段
            if (taskName == null || taskName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", 400,
                        "message", "任务名称不能为空"
                ));
            }

            // 设置默认值
            String finalSystemType = (systemType == null || systemType.trim().isEmpty())
                    ? "INSPECTION" : systemType.toUpperCase();

            // 创建数据对象
            InspectionData data = new InspectionData();
            data.setTaskName(taskName);
            data.setSystemType(finalSystemType);
            data.setAreaId(areaId);

            // ⚠️ 重要：PENDING 状态的任务不分配无人机，所以这里不要设置 droneId
            // 只有当任务状态变为 EXECUTING 时才应该分配无人机

            data.setUploaderId(1); // 假设当前用户ID为1，实际应该从session/token获取
            data.setDescription(description);
            data.setResponseTime(responseTime != null ? responseTime : 0);
            data.setResolutionTime(0); // 初始解决时间为0
            data.setAdditionalData(additionalData);

            // 设置严重程度
            if (severityLevel != null && !severityLevel.trim().isEmpty()) {
                data.setSeverityLevel(severityLevel);
            } else {
                // 根据系统类型设置默认严重程度
                switch (finalSystemType) {
                    case "MEDICAL":
                        data.setSeverityLevel("CRITICAL");
                        break;
                    case "POWER":
                        data.setSeverityLevel("MEDIUM");
                        break;
                    case "FOREST":
                        data.setSeverityLevel("HIGH");
                        break;
                    default:
                        data.setSeverityLevel("NORMAL");
                }
            }

            // ⚠️ 重要：新创建的任务状态应为 PENDING，不分配无人机
            data.setStatus(InspectionData.Status.PENDING);

            // 设置数据类型（如果有文件则根据文件类型设置，否则为OTHER）
            if (files != null && files.length > 0 && files[0] != null && !files[0].isEmpty()) {
                String fileName = files[0].getOriginalFilename();
                if (fileName != null) {
                    if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg") || fileName.toLowerCase().endsWith(".png")) {
                        data.setDataType(InspectionData.DataType.PHOTO);
                    } else if (fileName.toLowerCase().endsWith(".mp4") || fileName.toLowerCase().endsWith(".avi") || fileName.toLowerCase().endsWith(".mov")) {
                        data.setDataType(InspectionData.DataType.VIDEO);
                    } else {
                        data.setDataType(InspectionData.DataType.OTHER);
                    }
                }
            } else {
                data.setDataType(InspectionData.DataType.OTHER);
            }

            Map<String, Object> result = new HashMap<>();

            // 处理文件上传
            if (files != null && files.length > 0 && files[0] != null && !files[0].isEmpty()) {
                try {
                    String storageBase = "E:/java/java-test1/vehicle/storage/uploads";
                    InspectionData saved = inspectionDataService.createDataWithFiles(data, files, storageBase);

                    result.put("id", saved.getId());
                    result.put("taskName", saved.getTaskName());
                    result.put("systemType", saved.getSystemType());
                    result.put("status", saved.getStatus());
                    result.put("severityLevel", saved.getSeverityLevel());
                    result.put("fileCount", saved.getFileCount());
                    result.put("message", "任务创建成功，已上传文件");
                } catch (Exception fileEx) {
                    fileEx.printStackTrace();
                    return ResponseEntity.status(500).body(Map.of(
                            "success", false,
                            "code", 500,
                            "message", "文件上传失败: " + fileEx.getMessage()
                    ));
                }
            } else {
                // 无文件，直接保存到数据库
                try {
                    data.setFileCount(0);
                    InspectionData saved = inspectionDataService.saveData(data);

                    result.put("id", saved.getId());
                    result.put("taskName", saved.getTaskName());
                    result.put("systemType", saved.getSystemType());
                    result.put("status", saved.getStatus());
                    result.put("severityLevel", saved.getSeverityLevel());
                    result.put("fileCount", saved.getFileCount());
                    result.put("message", "任务创建成功");

                    System.out.println("✅ 任务已保存到数据库，ID: " + saved.getId());
                } catch (Exception dbEx) {
                    dbEx.printStackTrace();
                    return ResponseEntity.status(500).body(Map.of(
                            "success", false,
                            "code", 500,
                            "message", "数据库保存失败: " + dbEx.getMessage()
                    ));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", 200,
                    "message", "任务创建成功",
                    "data", result
            ));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", 500,
                    "message", "系统内部错误: " + ex.getMessage()
            ));
        }
    }

    // 快速创建医疗急救任务 - 修复：真正保存到数据库
    @PostMapping("/medical/emergency")
    public ResponseEntity<?> createMedicalEmergency(
            @RequestBody Map<String, Object> requestBody) {

        try {
            System.out.println("🚑 接收医疗急救请求: " + requestBody);

            // 获取参数
            String patientCondition = (String) requestBody.get("patientCondition");
            String location = (String) requestBody.get("location");
            String requiredMedication = (String) requestBody.get("requiredMedication");
            String severityLevel = (String) requestBody.getOrDefault("severityLevel", "CRITICAL");
            Integer responseTime = requestBody.get("responseTime") != null ?
                    Integer.parseInt(requestBody.get("responseTime").toString()) : 10;
            Integer areaId = requestBody.get("areaId") != null ?
                    Integer.parseInt(requestBody.get("areaId").toString()) : null;

            // 验证必填字段
            if (patientCondition == null || patientCondition.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", 400,
                        "message", "患者状况不能为空"
                ));
            }

            // 设置默认值
            if (location == null || location.trim().isEmpty()) {
                location = "未知地点";
            }

            if (requiredMedication == null || requiredMedication.trim().isEmpty()) {
                requiredMedication = "待定药品";
            }

            String taskName = "医疗急救 - " + patientCondition + " - " + location;
            String description = "患者状况: " + patientCondition +
                    "\n地点: " + location +
                    "\n所需药品: " + requiredMedication;

            // 构建additionalData JSON
            String additionalData = String.format(
                    "{\"patientCondition\": \"%s\", \"location\": \"%s\", \"requiredMedication\": \"%s\"}",
                    patientCondition, location, requiredMedication
            );

            // 创建并保存数据
            InspectionData data = new InspectionData();
            data.setTaskName(taskName);
            data.setSystemType("MEDICAL");
            data.setAreaId(areaId);
            // ⚠️ 不设置 droneId，任务状态为 PENDING
            data.setUploaderId(1); // 假设当前用户
            data.setDescription(description);
            data.setSeverityLevel(severityLevel);
            data.setResponseTime(responseTime);
            data.setResolutionTime(0);
            data.setAdditionalData(additionalData);
            data.setDataType(InspectionData.DataType.OTHER);
            data.setStatus(InspectionData.Status.PENDING); // ⚠️ 新任务为 PENDING 状态
            data.setFileCount(0);

            // 保存到数据库
            InspectionData saved = inspectionDataService.saveData(data);

            Map<String, Object> result = new HashMap<>();
            result.put("id", saved.getId());
            result.put("taskName", saved.getTaskName());
            result.put("systemType", saved.getSystemType());
            result.put("severityLevel", saved.getSeverityLevel());
            result.put("responseTime", saved.getResponseTime() + "分钟");
            result.put("location", location);
            result.put("patientCondition", patientCondition);
            result.put("requiredMedication", requiredMedication);
            result.put("status", saved.getStatus());
            result.put("message", "医疗急救任务已创建成功");

            System.out.println("✅ 医疗急救任务已保存到数据库，ID: " + saved.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", 200,
                    "message", "医疗急救任务创建成功",
                    "data", result
            ));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", 500,
                    "message", "创建医疗急救任务失败: " + ex.getMessage()
            ));
        }
    }

    // 快速创建电力巡检任务 - 修复：真正保存到数据库
    @PostMapping("/power/inspection")
    public ResponseEntity<?> createPowerInspection(
            @RequestBody Map<String, Object> requestBody) {

        try {
            System.out.println("⚡ 接收电力巡检请求: " + requestBody);

            String lineType = (String) requestBody.getOrDefault("lineType", "输电线路");
            String voltageLevel = (String) requestBody.getOrDefault("voltageLevel", "220kV");
            String inspectionType = (String) requestBody.getOrDefault("inspectionType", "常规巡检");
            String issueType = (String) requestBody.get("issueType");
            String location = (String) requestBody.get("location");
            Integer areaId = requestBody.get("areaId") != null ?
                    Integer.parseInt(requestBody.get("areaId").toString()) : null;
            String severityLevel = (String) requestBody.getOrDefault("severityLevel", "MEDIUM");
            Integer responseTime = requestBody.get("responseTime") != null ?
                    Integer.parseInt(requestBody.get("responseTime").toString()) : 30;

            String taskName = "电力巡检 - " + voltageLevel + " - " + lineType;
            if (location != null && !location.trim().isEmpty()) {
                taskName += " - " + location;
            }

            String description = "线路类型: " + lineType +
                    "\n电压等级: " + voltageLevel +
                    "\n巡检类型: " + inspectionType;

            if (issueType != null && !issueType.trim().isEmpty()) {
                description += "\n问题类型: " + issueType;
            }
            if (location != null && !location.trim().isEmpty()) {
                description += "\n地点: " + location;
            }

            // 构建additionalData
            StringBuilder additionalDataBuilder = new StringBuilder();
            additionalDataBuilder.append("{");
            additionalDataBuilder.append("\"lineType\":\"").append(lineType).append("\",");
            additionalDataBuilder.append("\"voltageLevel\":\"").append(voltageLevel).append("\",");
            additionalDataBuilder.append("\"inspectionType\":\"").append(inspectionType).append("\"");
            if (issueType != null && !issueType.trim().isEmpty()) {
                additionalDataBuilder.append(",\"issueType\":\"").append(issueType).append("\"");
            }
            if (location != null && !location.trim().isEmpty()) {
                additionalDataBuilder.append(",\"location\":\"").append(location).append("\"");
            }
            additionalDataBuilder.append("}");

            // 创建并保存数据
            InspectionData data = new InspectionData();
            data.setTaskName(taskName);
            data.setSystemType("POWER");
            data.setAreaId(areaId);
            // ⚠️ 不设置 droneId
            data.setUploaderId(1);
            data.setDescription(description);
            data.setSeverityLevel(severityLevel);
            data.setResponseTime(responseTime);
            data.setResolutionTime(0);
            data.setAdditionalData(additionalDataBuilder.toString());
            data.setDataType(InspectionData.DataType.OTHER);
            data.setStatus(InspectionData.Status.PENDING); // ⚠️ 新任务为 PENDING 状态
            data.setFileCount(0);

            // 保存到数据库
            InspectionData saved = inspectionDataService.saveData(data);

            Map<String, Object> result = new HashMap<>();
            result.put("id", saved.getId());
            result.put("taskName", saved.getTaskName());
            result.put("systemType", saved.getSystemType());
            result.put("voltageLevel", voltageLevel);
            result.put("lineType", lineType);
            result.put("inspectionType", inspectionType);
            result.put("severityLevel", saved.getSeverityLevel());
            result.put("status", saved.getStatus());
            result.put("message", "电力巡检任务已创建");

            System.out.println("✅ 电力巡检任务已保存到数据库，ID: " + saved.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", 200,
                    "data", result
            ));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", 500,
                    "message", "创建电力巡检任务失败: " + ex.getMessage()
            ));
        }
    }

    // 分配无人机到任务（将任务状态改为 EXECUTING 并分配无人机）
    @PutMapping("/task/{id}/assign-uav")
    public ResponseEntity<?> assignUavToTask(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> requestBody) {

        try {
            String droneId = (String) requestBody.get("droneId");
            String status = (String) requestBody.getOrDefault("status", "EXECUTING");

            if (droneId == null || droneId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", 400,
                        "message", "无人机ID不能为空"
                ));
            }

            // 获取任务
            InspectionData task = inspectionDataService.getById(id);
            if (task == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "code", 404,
                        "message", "任务不存在"
                ));
            }

            // 更新任务状态和无人机ID
            task.setDroneId(droneId);
            task.setStatus(InspectionData.Status.fromCode(status));
            task.setUpdatedAt(LocalDateTime.now());

            // 保存更新
            boolean updated = inspectionDataService.updateData(task);

            if (updated) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", task.getId());
                result.put("taskName", task.getTaskName());
                result.put("droneId", task.getDroneId());
                result.put("status", task.getStatus());
                result.put("updatedAt", task.getUpdatedAt());
                result.put("message", "无人机分配成功");

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "code", 200,
                        "data", result
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                        "success", false,
                        "code", 500,
                        "message", "更新任务失败"
                ));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", 500,
                    "message", "分配无人机失败: " + ex.getMessage()
            ));
        }
    }

    // 更新任务状态（但不分配无人机）
    @PutMapping("/task/{id}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable("id") Long id,
            @RequestBody Map<String, Object> requestBody) {

        try {
            String status = (String) requestBody.get("status");
            Integer resolutionTime = requestBody.get("resolutionTime") != null ?
                    Integer.parseInt(requestBody.get("resolutionTime").toString()) : null;

            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "code", 400,
                        "message", "状态不能为空"
                ));
            }

            // 获取任务
            InspectionData task = inspectionDataService.getById(id);
            if (task == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "code", 404,
                        "message", "任务不存在"
                ));
            }

            // 更新任务状态
            task.setStatus(InspectionData.Status.fromCode(status));
            if (resolutionTime != null) {
                task.setResolutionTime(resolutionTime);
            }
            task.setUpdatedAt(LocalDateTime.now());

            // 保存更新
            boolean updated = inspectionDataService.updateData(task);

            if (updated) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", task.getId());
                result.put("taskName", task.getTaskName());
                result.put("status", task.getStatus());
                result.put("resolutionTime", task.getResolutionTime());
                result.put("updatedAt", task.getUpdatedAt());
                result.put("message", "状态更新成功");

                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "code", 200,
                        "data", result
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                        "success", false,
                        "code", 500,
                        "message", "更新任务失败"
                ));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", 500,
                    "message", "更新状态失败: " + ex.getMessage()
            ));
        }
    }

    // 在 MultiSystemController 中添加智能森林任务接口
    @PostMapping("/forest/task")
    public ResponseEntity<?> createForestTask(
            @RequestBody Map<String, Object> requestBody) {

        try {
            System.out.println("🌲 接收森林任务请求: " + requestBody);

            // 1. 获取公共参数
            String taskType = (String) requestBody.getOrDefault("taskType", "FIRE_MONITORING");
            String location = (String) requestBody.getOrDefault("location", "林区");
            Integer areaId = requestBody.get("areaId") != null ?
                    Integer.parseInt(requestBody.get("areaId").toString()) : null;
            String severityLevel = (String) requestBody.getOrDefault("severityLevel", "NORMAL");
            Integer responseTime = requestBody.get("responseTime") != null ?
                    Integer.parseInt(requestBody.get("responseTime").toString()) : 60;
            String description = (String) requestBody.get("description");
            String customTaskName = (String) requestBody.get("taskName");

            // 2. 根据任务类型构建不同的任务
            String taskName = "";
            String fullDescription = "";
            String additionalData = "";
            InspectionData.DataType dataType = InspectionData.DataType.PHOTO;

            switch (taskType.toUpperCase()) {
                case "FIRE_MONITORING":
                    // 火险监测任务
                    String riskLevel = (String) requestBody.getOrDefault("riskLevel", "中");
                    Integer temperature = requestBody.get("temperature") != null ?
                            Integer.parseInt(requestBody.get("temperature").toString()) : 30;
                    Integer humidity = requestBody.get("humidity") != null ?
                            Integer.parseInt(requestBody.get("humidity").toString()) : 40;

                    taskName = customTaskName != null ? customTaskName :
                            "森林火险监测 - " + location;
                    fullDescription = description != null ? description :
                            "监测地点: " + location +
                                    "\n风险等级: " + riskLevel +
                                    "\n当前温度: " + temperature + "°C" +
                                    "\n湿度: " + humidity + "%";
                    additionalData = String.format(
                            "{\"taskType\": \"FIRE_MONITORING\", \"location\": \"%s\", \"riskLevel\": \"%s\", \"temperature\": %d, \"humidity\": %d}",
                            location, riskLevel, temperature, humidity
                    );
                    severityLevel = requestBody.get("severityLevel") != null ?
                            severityLevel : "HIGH";
                    responseTime = requestBody.get("responseTime") != null ?
                            responseTime : 15;
                    break;

                case "PEST_MONITORING":
                    // 病虫害监测任务
                    String pestType = (String) requestBody.getOrDefault("pestType", "松材线虫");
                    String treeSpecies = (String) requestBody.getOrDefault("treeSpecies", "马尾松");
                    String affectedArea = (String) requestBody.getOrDefault("affectedArea", "10公顷");

                    taskName = customTaskName != null ? customTaskName :
                            "森林病虫害监测 - " + pestType + " - " + location;
                    fullDescription = description != null ? description :
                            "监测地点: " + location +
                                    "\n病虫害类型: " + pestType +
                                    "\n树种: " + treeSpecies +
                                    "\n影响面积: " + affectedArea;
                    additionalData = String.format(
                            "{\"taskType\": \"PEST_MONITORING\", \"location\": \"%s\", \"pestType\": \"%s\", \"treeSpecies\": \"%s\", \"affectedArea\": \"%s\"}",
                            location, pestType, treeSpecies, affectedArea
                    );
                    severityLevel = requestBody.get("severityLevel") != null ?
                            severityLevel : "MEDIUM";
                    responseTime = requestBody.get("responseTime") != null ?
                            responseTime : 60;
                    break;

                case "WILDLIFE_MONITORING":
                    // 野生动物监测任务
                    String animalType = (String) requestBody.getOrDefault("animalType", "候鸟");
                    String monitoringPeriod = (String) requestBody.getOrDefault("monitoringPeriod", "春季");
                    Integer population = requestBody.get("population") != null ?
                            Integer.parseInt(requestBody.get("population").toString()) : null;

                    taskName = customTaskName != null ? customTaskName :
                            "野生动物监测 - " + animalType + " - " + location;
                    fullDescription = description != null ? description :
                            "监测地点: " + location +
                                    "\n动物类型: " + animalType +
                                    "\n监测时期: " + monitoringPeriod;

                    // 构建带有可选种群的additionalData
                    StringBuilder wildlifeData = new StringBuilder();
                    wildlifeData.append(String.format(
                            "{\"taskType\": \"WILDLIFE_MONITORING\", \"location\": \"%s\", \"animalType\": \"%s\", \"monitoringPeriod\": \"%s\"",
                            location, animalType, monitoringPeriod
                    ));
                    if (population != null) {
                        wildlifeData.append(String.format(", \"population\": %d", population));
                    }
                    wildlifeData.append("}");
                    additionalData = wildlifeData.toString();

                    dataType = InspectionData.DataType.VIDEO;
                    severityLevel = requestBody.get("severityLevel") != null ?
                            severityLevel : "NORMAL";
                    responseTime = requestBody.get("responseTime") != null ?
                            responseTime : 120;
                    break;

                default:
                    // 默认任务（自定义任务）
                    taskName = customTaskName != null ? customTaskName :
                            "森林巡护任务 - " + location;
                    fullDescription = description != null ? description :
                            "森林巡护任务 - " + location;
                    additionalData = String.format(
                            "{\"taskType\": \"CUSTOM\", \"location\": \"%s\"}",
                            location
                    );
            }

            // 3. 创建并保存数据
            InspectionData data = new InspectionData();
            data.setTaskName(taskName);
            data.setSystemType("FOREST");
            data.setAreaId(areaId);
            data.setUploaderId(1); // 假设当前用户
            data.setDescription(fullDescription);
            data.setSeverityLevel(severityLevel);
            data.setResponseTime(responseTime);
            data.setResolutionTime(0);
            data.setAdditionalData(additionalData);
            data.setDataType(dataType);
            data.setStatus(InspectionData.Status.PENDING);
            data.setFileCount(0);

            // 保存到数据库
            InspectionData saved = inspectionDataService.saveData(data);

            // 4. 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("id", saved.getId());
            result.put("taskName", saved.getTaskName());
            result.put("taskType", taskType.toUpperCase());
            result.put("systemType", saved.getSystemType());
            result.put("location", location);
            result.put("severityLevel", saved.getSeverityLevel());
            result.put("responseTime", saved.getResponseTime());
            result.put("status", saved.getStatus());
            result.put("message", "森林任务创建成功");

            // 添加任务类型特定的信息
            switch (taskType.toUpperCase()) {
                case "FIRE_MONITORING":
                    result.put("riskLevel", requestBody.getOrDefault("riskLevel", "中"));
                    result.put("temperature", requestBody.get("temperature"));
                    result.put("humidity", requestBody.get("humidity"));
                    break;
                case "PEST_MONITORING":
                    result.put("pestType", requestBody.getOrDefault("pestType", "松材线虫"));
                    result.put("treeSpecies", requestBody.getOrDefault("treeSpecies", "马尾松"));
                    result.put("affectedArea", requestBody.getOrDefault("affectedArea", "10公顷"));
                    break;
                case "WILDLIFE_MONITORING":
                    result.put("animalType", requestBody.getOrDefault("animalType", "候鸟"));
                    result.put("monitoringPeriod", requestBody.getOrDefault("monitoringPeriod", "春季"));
                    result.put("population", requestBody.get("population"));
                    break;
            }

            System.out.println("✅ 森林任务已保存到数据库，ID: " + saved.getId());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", 200,
                    "message", "森林任务创建成功",
                    "data", result
            ));

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", 500,
                    "message", "创建森林任务失败: " + ex.getMessage()
            ));
        }
    }
    // 获取系统任务列表（按系统类型筛选）
    @GetMapping("/tasks")
    public ResponseEntity<?> getSystemTasks(
            @RequestParam(required = false) String systemType,
            @RequestParam(required = false) String severityLevel,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        try {
            Map<String, Object> params = new HashMap<>();
            if (systemType != null && !systemType.isEmpty()) {
                params.put("systemType", systemType);
            }
            if (severityLevel != null && !severityLevel.isEmpty()) {
                params.put("severityLevel", severityLevel);
            }
            if (status != null && !status.isEmpty()) {
                params.put("status", status);
            }
            params.put("offset", page * size);
            params.put("limit", size);

            List<InspectionData> tasks = inspectionDataService.queryList(params);

            // 统计数据
            Map<String, Object> result = new HashMap<>();
            result.put("tasks", tasks);
            result.put("total", tasks.size());
            result.put("page", page);
            result.put("size", size);
            result.put("success", true);
            result.put("code", 200);

            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", 500,
                    "error", ex.getMessage()
            ));
        }
    }

    // 获取任务详情
    @GetMapping("/task/{id}")
    public ResponseEntity<?> getTaskDetail(@PathVariable("id") Long id) {
        try {
            InspectionData task = inspectionDataService.getById(id);
            if (task == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "code", 404,
                        "message", "任务不存在"
                ));
            }

            // 获取关联的媒体文件
            List<?> media = inspectionDataService.getMediaByDataId(id);

            Map<String, Object> detail = new HashMap<>();
            detail.put("id", task.getId());
            detail.put("taskName", task.getTaskName());
            detail.put("systemType", task.getSystemType());
            detail.put("severityLevel", task.getSeverityLevel());
            detail.put("description", task.getDescription());
            detail.put("status", task.getStatus());
            detail.put("createdAt", task.getCreatedAt());
            detail.put("responseTime", task.getResponseTime());
            detail.put("areaId", task.getAreaId());
            detail.put("droneId", task.getDroneId());

            if (task.getAdditionalData() != null) {
                detail.put("additionalData", task.getAdditionalData());
            }

            if (media != null && !media.isEmpty()) {
                detail.put("media", media);
                detail.put("mediaCount", media.size());
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", 200,
                    "data", detail
            ));

        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", 500,
                    "error", ex.getMessage()
            ));
        }
    }


    // 获取系统统计信息
    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        try {
            Map<String, Object> stats = new HashMap<>();

            // 这里需要从数据库获取统计数据
            // 暂时返回模拟数据
            stats.put("medical", Map.of(
                    "total", 15,
                    "critical", 5,
                    "high", 7,
                    "normal", 3
            ));

            stats.put("power", Map.of(
                    "total", 23,
                    "critical", 8,
                    "high", 10,
                    "medium", 5
            ));

            stats.put("forest", Map.of(
                    "total", 12,
                    "high", 4,
                    "normal", 8
            ));

            stats.put("inspection", Map.of(
                    "total", 45,
                    "completed", 30,
                    "executing", 10,
                    "pending", 5
            ));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", 200,
                    "data", stats
            ));

        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "code", 500,
                    "error", ex.getMessage()
            ));
        }
    }
}