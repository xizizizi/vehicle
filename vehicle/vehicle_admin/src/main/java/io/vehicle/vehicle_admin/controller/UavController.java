package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.entity.Uav;
import io.vehicle.vehicle_admin.service.UavService;
import io.vehicle.vehicle_admin.vo.*;
import io.vehicle.vehicle_admin.config.NanChangLocationConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/uav-manage")  // 改为不同的路径，避免冲突
public class UavController {

    @Autowired
    private UavService uavService;

    @Autowired
    private NanChangLocationConfig nanChangLocationConfig;

    /**
     * 获取所有无人机 - 使用 /api/uav-manage/all
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllUavs() {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Uav> uavs = uavService.getAllUavs();
            result.put("success", true);
            result.put("data", uavs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取无人机列表失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 添加无人机
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addUav(@RequestBody UavAddVO uavAddVO) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 手动验证
            String validationError = uavAddVO.validate();
            if (validationError != null) {
                result.put("success", false);
                result.put("message", validationError);
                return ResponseEntity.badRequest().body(result);
            }

            Uav uav = uavService.addUav(uavAddVO);
            result.put("success", true);
            result.put("message", "添加无人机成功");
            result.put("data", uav);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("添加无人机失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新无人机
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<Map<String, Object>> updateUav(
            @PathVariable Integer id,
            @RequestBody UavUpdateVO uavUpdateVO) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 手动验证
            String validationError = uavUpdateVO.validate();
            if (validationError != null) {
                result.put("success", false);
                result.put("message", validationError);
                return ResponseEntity.badRequest().body(result);
            }

            Uav uav = uavService.updateUav(id, uavUpdateVO);
            result.put("success", true);
            result.put("message", "更新无人机成功");
            result.put("data", uav);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("更新无人机失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 删除无人机
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteUav(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();

        try {
            boolean success = uavService.deleteUav(id);
            result.put("success", success);
            result.put("message", success ? "删除无人机成功" : "删除无人机失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("删除无人机失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取无人机详情
     */
    @GetMapping("/detail/{id}")
    public ResponseEntity<Map<String, Object>> getUav(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();

        try {
            Uav uav = uavService.getUavById(id);
            if (uav == null) {
                result.put("success", false);
                result.put("message", "无人机不存在");
                return ResponseEntity.badRequest().body(result);
            }
            result.put("success", true);
            result.put("data", uav);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取无人机详情失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 根据状态查询无人机
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getUavsByStatus(
            @PathVariable String status) {
        Map<String, Object> result = new HashMap<>();

        try {
            Uav.UavStatus uavStatus;
            try {
                uavStatus = Uav.UavStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                result.put("success", false);
                result.put("message", "无效的状态值，有效值：IDLE, CHARGING, ON_MISSION, MAINTENANCE, ERROR");
                return ResponseEntity.badRequest().body(result);
            }

            List<Uav> uavs = uavService.getUavsByStatus(uavStatus);
            result.put("success", true);
            result.put("data", uavs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("根据状态查询无人机失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 搜索无人机
     */
    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUavs(
            @RequestBody UavQueryVO queryVO) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Uav> uavs = uavService.searchUavs(queryVO);
            result.put("success", true);
            result.put("data", uavs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("搜索无人机失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取无人机统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUavStatistics() {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> statistics = uavService.getUavStatistics();
            result.put("success", true);
            result.put("data", statistics);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取无人机统计失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 批量导入无人机
     */
    @PostMapping("/batch-import")
    public ResponseEntity<Map<String, Object>> batchImportUavs(
            @RequestBody List<UavAddVO> uavAddVOs) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证每个对象
            for (int i = 0; i < uavAddVOs.size(); i++) {
                String validationError = uavAddVOs.get(i).validate();
                if (validationError != null) {
                    result.put("success", false);
                    result.put("message", String.format("第%d行数据验证失败: %s", i + 1, validationError));
                    return ResponseEntity.badRequest().body(result);
                }
            }

            List<Uav> uavs = uavService.batchImportUavs(uavAddVOs);
            result.put("success", true);
            result.put("message", String.format("成功导入 %d 台无人机", uavs.size()));
            result.put("data", uavs);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("批量导入无人机失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新无人机位置
     */
    @PutMapping("/location/{id}")
    public ResponseEntity<Map<String, Object>> updateUavLocation(
            @PathVariable Integer id,
            @RequestParam Double lng,
            @RequestParam Double lat) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证经纬度
            if (lng < 115.7 || lng > 116.1) {
                result.put("success", false);
                result.put("message", "经度应在115.7-116.1之间（南昌范围）");
                return ResponseEntity.badRequest().body(result);
            }
            if (lat < 28.5 || lat > 28.9) {
                result.put("success", false);
                result.put("message", "纬度应在28.5-28.9之间（南昌范围）");
                return ResponseEntity.badRequest().body(result);
            }

            boolean success = uavService.updateUavLocation(id, lng, lat);
            result.put("success", success);
            result.put("message", success ? "更新位置成功" : "更新位置失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("更新无人机位置失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新无人机状态
     */
    @PutMapping("/status/{id}")
    public ResponseEntity<Map<String, Object>> updateUavStatus(
            @PathVariable Integer id,
            @RequestParam String status) {
        Map<String, Object> result = new HashMap<>();

        try {
            Uav.UavStatus uavStatus;
            try {
                uavStatus = Uav.UavStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                result.put("success", false);
                result.put("message", "无效的状态值，有效值：IDLE, CHARGING, ON_MISSION, MAINTENANCE, ERROR");
                return ResponseEntity.badRequest().body(result);
            }

            boolean success = uavService.updateUavStatus(id, uavStatus);
            result.put("success", success);
            result.put("message", success ? "更新状态成功" : "更新状态失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("更新无人机状态失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新无人机电量
     */
    @PutMapping("/battery/{id}")
    public ResponseEntity<Map<String, Object>> updateUavBattery(
            @PathVariable Integer id,
            @RequestParam Integer batteryLevel) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 验证电量
            if (batteryLevel < 0 || batteryLevel > 100) {
                result.put("success", false);
                result.put("message", "电量必须在0-100之间");
                return ResponseEntity.badRequest().body(result);
            }

            boolean success = uavService.updateUavBattery(id, batteryLevel);
            result.put("success", success);
            result.put("message", success ? "更新电量成功" : "更新电量失败");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("更新无人机电量失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 获取南昌地点列表（用于下拉选择）
     */
    @GetMapping("/nanchang-locations")
    public ResponseEntity<Map<String, Object>> getNanchangLocations() {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, NanChangLocationConfig.Location> locations = nanChangLocationConfig.getLocations();
            result.put("success", true);
            result.put("data", locations);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取南昌地点列表失败", e);
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}