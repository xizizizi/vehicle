package io.vehicle.vehicle_admin.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.vehicle.vehicle_admin.config.NanChangLocationConfig;
import io.vehicle.vehicle_admin.entity.*;
import io.vehicle.vehicle_admin.mapper.CruiseMissionMapper;
import io.vehicle.vehicle_admin.mapper.InspectionDataMapper;
import io.vehicle.vehicle_admin.mapper.OrderMapper;
import io.vehicle.vehicle_admin.mapper.UavMapper;
import io.vehicle.vehicle_admin.service.DispatchService;
import io.vehicle.vehicle_admin.service.RoutePlanningService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api")
//@CrossOrigin(origins = "*")
public class DispatchController {

    @Autowired
    private UavMapper uavMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CruiseMissionMapper missionMapper;

    @Autowired
    private DispatchService dispatchService;

    @Autowired
    private NanChangLocationConfig locationConfig;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private InspectionDataMapper inspectionDataMapper;  // 需要添加这个依赖
    // 添加统一的异常处理
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        log.error("控制器异常: ", e);
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "服务器内部错误: " + e.getMessage());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    /**
     * 获取所有无人机
     */
    @GetMapping("/uavs")
    public Map<String, Object> getAllUavs() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("获取所有无人机数据");
            LambdaQueryWrapper<Uav> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Uav::getDeleted, 0);
            List<Uav> uavs = uavMapper.selectList(queryWrapper);

            result.put("success", true);
            result.put("data", uavs);
            result.put("count", uavs.size());
            log.info("返回无人机数据: {} 条", uavs.size());

        } catch (Exception e) {
            log.error("获取无人机数据失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "获取无人机数据失败");
        }

        return result;
    }

    /**
     * 获取所有订单
     */
    @GetMapping("/orders")
    public Map<String, Object> getAllOrders() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("获取所有订单数据");
            LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Order::getDeleted, 0)
                    .orderByDesc(Order::getCreatedTime);
            List<Order> orders = orderMapper.selectList(queryWrapper);

            result.put("success", true);
            result.put("data", orders);
            result.put("count", orders.size());
            log.info("返回订单数据: {} 条", orders.size());

        } catch (Exception e) {
            log.error("获取订单数据失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "获取订单数据失败");
        }

        return result;
    }

    /**
     * 获取所有任务
     */
    @GetMapping("/missions")
    public Map<String, Object> getAllMissions() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("获取所有任务数据");
            LambdaQueryWrapper<CruiseMission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CruiseMission::getDeleted, 0)
                    .orderByDesc(CruiseMission::getCreatedTime);
            List<CruiseMission> missions = missionMapper.selectList(queryWrapper);

            result.put("success", true);
            result.put("data", missions);
            result.put("count", missions.size());
            log.info("返回任务数据: {} 条", missions.size());

        } catch (Exception e) {
            log.error("获取任务数据失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "获取任务数据失败");
        }

        return result;
    }

    /**
     * 获取关键位置
     */
    @GetMapping("/locations")
    public Map<String, Object> getKeyLocations() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("获取关键位置数据");
            Map<String, NanChangLocationConfig.Location> locations = locationConfig.getLocations();

            // 如果locations为空，提供默认数据
            if (locations == null || locations.isEmpty()) {
                log.warn("配置的位置数据为空，使用默认数据");
                locations = getDefaultLocations();
            }

            result.put("success", true);
            result.put("data", locations);
            result.put("count", locations.size());
            log.info("返回位置数据: {} 个", locations.size());

        } catch (Exception e) {
            log.error("获取位置数据失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "获取位置数据失败");
        }

        return result;
    }

    /**
     * 提供默认位置数据
     */
    private Map<String, NanChangLocationConfig.Location> getDefaultLocations() {
        Map<String, NanChangLocationConfig.Location> defaultLocations = new HashMap<>();
        defaultLocations.put("南昌站", new NanChangLocationConfig.Location(115.907, 28.662));
        defaultLocations.put("南昌西站", new NanChangLocationConfig.Location(115.768, 28.620));
        defaultLocations.put("昌北机场", new NanChangLocationConfig.Location(115.900, 28.865));
        defaultLocations.put("南昌东站", new NanChangLocationConfig.Location(115.983, 28.625));
        defaultLocations.put("徐坊客运站", new NanChangLocationConfig.Location(115.889, 28.663));
        defaultLocations.put("八一广场", new NanChangLocationConfig.Location(115.899, 28.679));
        return defaultLocations;
    }
    /**
     * 创建快递订单
     */
    @PostMapping("/orders/express")
    public Map<String, Object> createExpressOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("创建快递订单请求: {}", request);

            // 参数验证
            if (request == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }

            String fromLocation = (String) request.get("fromLocation");
            String toLocation = (String) request.get("toLocation");
            Object weightObj = request.get("weight");

            log.info("解析参数 - fromLocation: {}, toLocation: {}, weight: {}",
                    fromLocation, toLocation, weightObj);

            if (fromLocation == null || toLocation == null || weightObj == null) {
                throw new IllegalArgumentException("缺少必要参数: fromLocation, toLocation, weight");
            }

            Double weight;
            try {
                weight = Double.valueOf(weightObj.toString());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("weight参数格式错误: " + weightObj);
            }

            log.info("开始创建订单: {} -> {}, 重量: {}kg", fromLocation, toLocation, weight);

            Order order = dispatchService.createExpressOrder(fromLocation, toLocation, weight);

            result.put("success", true);
            result.put("data", order);
            result.put("message", "订单创建成功");
            log.info("订单创建成功: orderId={}", order.getId());

        } catch (Exception e) {
            log.error("创建快递订单失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "创建订单失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 创建巡航任务
     */
    @PostMapping("/missions/cruise")
    public Map<String, Object> createCruiseMission(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("创建巡航任务: {}", request);
            String missionName = (String) request.get("missionName");
            Object routePointsObj = request.get("routePoints");

            // 修复类型转换问题
            JSONArray routePoints;
            if (routePointsObj instanceof java.util.List) {
                // 如果是List，转换为JSONArray
                routePoints = new JSONArray((java.util.List<?>) routePointsObj);
            } else if (routePointsObj instanceof JSONArray) {
                // 如果已经是JSONArray，直接使用
                routePoints = (JSONArray) routePointsObj;
            } else {
                throw new IllegalArgumentException("routePoints格式不正确，应为数组");
            }

            log.info("任务名称: {}, 路径点数量: {}", missionName, routePoints.size());

            CruiseMission mission = dispatchService.createCruiseMission(missionName, routePoints);

            result.put("success", true);
            result.put("data", mission);
            result.put("message", "任务创建成功");

        } catch (Exception e) {
            log.error("创建巡航任务失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "创建任务失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 创建电力巡检巡航任务（修复版）
     */
    @PostMapping("/missions/power-cruise")
    public Map<String, Object> createPowerCruiseMission(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("创建电力巡航任务: {}", request);

            // 获取任务参数
            String missionName = (String) request.get("missionName");
            String missionType = (String) request.get("missionType");
            String systemType = (String) request.get("systemType");

            Long inspectionDataId = null;
            if (request.get("inspectionDataId") != null) {
                try {
                    inspectionDataId = Long.valueOf(request.get("inspectionDataId").toString());
                } catch (NumberFormatException e) {
                    log.warn("inspectionDataId格式错误: {}", request.get("inspectionDataId"));
                    inspectionDataId = null;
                }
            }

            String severityLevel = (String) request.get("severityLevel");
            Integer responseTime = (Integer) request.get("responseTime");
            String droneSn = (String) request.get("droneSn");

            Integer assignedUavId = null;
            if (request.get("assignedUavId") != null) {
                try {
                    assignedUavId = Integer.valueOf(request.get("assignedUavId").toString());
                } catch (NumberFormatException e) {
                    log.warn("assignedUavId格式错误");
                }
            }

            // 获取路线点
            JSONArray routePoints;
            Object routePointsObj = request.get("routePoints");
            if (routePointsObj instanceof java.util.List) {
                routePoints = new JSONArray((java.util.List<?>) routePointsObj);
            } else if (routePointsObj instanceof JSONArray) {
                routePoints = (JSONArray) routePointsObj;
            } else {
                throw new IllegalArgumentException("routePoints格式不正确，应为数组");
            }

            log.info("创建电力任务 - 名称: {}, 类型: {}, 数据ID: {}, 无人机ID: {}, 无人机SN: {}",
                    missionName, systemType, inspectionDataId, assignedUavId, droneSn);

            // 1. 检查无人机是否存在且可用
            Uav uav = null;
            if (assignedUavId != null) {
                uav = uavMapper.selectById(assignedUavId);
                if (uav == null) {
                    throw new RuntimeException("未找到指定无人机: uavId=" + assignedUavId);
                }

                // 检查无人机状态
                if (!isUavAvailable(uav)) {
                    throw new RuntimeException("无人机 " + uav.getSn() + " 当前状态为 " + uav.getStatus() + "，不可用");
                }

                // 检查电量是否足够
                if (uav.getBatteryLevel() < 20) {
                    throw new RuntimeException("无人机电量过低(" + uav.getBatteryLevel() + "%)，请充电后使用");
                }
            }

            // 2. 创建巡航任务
            CruiseMission mission = new CruiseMission();
            mission.setMissionName(missionName);
            mission.setMissionType(missionType != null ?
                    CruiseMission.MissionType.valueOf(missionType) :
                    CruiseMission.MissionType.CRUISE);
            mission.setSystemType(systemType != null ? systemType : "POWER");
            mission.setSeverityLevel(severityLevel);
            mission.setResponseTime(responseTime != null ? responseTime : 30);
            mission.setMedicalDataId(inspectionDataId); // 使用medical_data_id字段存储inspection_data_id

            // 设置路线点 - 确保正确序列化
            mission.setRoutePoints(routePoints.toJSONString());

            // 如果指定了无人机，直接设置为已激活状态
            if (assignedUavId != null && uav != null) {
                mission.setAssignedUavId(assignedUavId);
                mission.setStatus(CruiseMission.MissionStatus.ACTIVE);
                mission.setStartTime(LocalDateTime.now());
            } else {
                mission.setStatus(CruiseMission.MissionStatus.PENDING);
            }

            mission.setPriority(1); // 电力巡检设置为高优先级
            mission.setCreatedTime(LocalDateTime.now());

            // 插入任务
            missionMapper.insert(mission);
            log.info("电力巡航任务创建成功: missionId={}, status={}", mission.getId(), mission.getStatus());

            // 3. 如果有分配的无人机，更新无人机状态
            if (assignedUavId != null && uav != null) {
                uav.setStatus(Uav.UavStatus.ON_MISSION);
                uav.setCurrentMission("MISSION_" + mission.getId());
                uav.setUpdatedTime(LocalDateTime.now());
                uavMapper.updateById(uav);
                log.info("无人机状态更新成功: uavId={}, sn={}, status=ON_MISSION", uav.getId(), uav.getSn());

                // 4. 更新巡检数据状态
                if (inspectionDataId != null) {
                    try {
                        InspectionData inspectionData = inspectionDataMapper.selectById(inspectionDataId);
                        if (inspectionData != null) {
                            inspectionData.setDroneId(droneSn != null ? droneSn : uav.getSn());

                            // 使用枚举类型设置状态
                            try {
                                InspectionData.Status status = InspectionData.Status.EXECUTING;  // 直接使用枚举值
                                inspectionData.setStatus(status);
                            } catch (Exception e) {
                                log.warn("设置状态失败，使用默认状态: {}", e.getMessage());
                                inspectionData.setStatus(InspectionData.Status.PENDING);
                            }

                            inspectionData.setUpdatedAt(LocalDateTime.now());
                            inspectionDataMapper.updateById(inspectionData);
                            log.info("巡检数据状态更新成功: dataId={}, droneId={}, status=EXECUTING",
                                    inspectionDataId, inspectionData.getDroneId());
                        } else {
                            log.warn("未找到巡检数据: dataId={}", inspectionDataId);
                        }
                    } catch (Exception e) {
                        log.error("更新巡检数据状态失败: {}", e.getMessage(), e);
                        // 不中断流程，仅记录错误
                    }
                }
            }
            result.put("success", true);
            result.put("data", mission);
            result.put("message", "电力巡航任务创建成功");
            log.info("电力巡航任务创建完成: missionId={}, assignedUavId={}", mission.getId(), assignedUavId);

        } catch (Exception e) {
            log.error("创建电力巡航任务失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "创建电力巡航任务失败: " + e.getMessage());
        }

        return result;
    }

    // 检查无人机是否可用
    private boolean isUavAvailable(Uav uav) {
        return uav.getStatus() == Uav.UavStatus.IDLE ||
                uav.getStatus() == Uav.UavStatus.CHARGING;
    }
    /**
     * 分配任务给无人机
     */
    @PostMapping("/missions/{missionId}/assign")
    public Map<String, Object> assignMission(@PathVariable Integer missionId,
                                             @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("分配任务: missionId={}, request={}", missionId, request);
            String uavSn = request.get("uavSn");
            boolean success = dispatchService.assignCruiseMission(missionId, uavSn);

            result.put("success", success);
            result.put("message", success ? "任务分配成功" : "任务分配失败");

        } catch (Exception e) {
            log.error("分配任务失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "分配任务失败");
        }

        return result;
    }
    /**
     * 获取无人机轨迹 - 支持ID和序列号
     */
    @GetMapping("/uavs/{identifier}/tracks")
    public Map<String, Object> getUavTracks(@PathVariable String identifier,
                                            @RequestParam(defaultValue = "100") Integer limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("获取无人机轨迹: identifier={}, limit={}", identifier, limit);

            Integer uavId = null;

            // 判断identifier是ID还是序列号
            if (identifier.startsWith("NCUAV_")) {
                // 如果是序列号，先查询无人机ID
                LambdaQueryWrapper<Uav> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(Uav::getSn, identifier).eq(Uav::getDeleted, 0);
                Uav uav = uavMapper.selectOne(queryWrapper);
                if (uav != null) {
                    uavId = uav.getId();
                } else {
                    throw new IllegalArgumentException("未找到序列号为 " + identifier + " 的无人机");
                }
            } else {
                // 如果是数字ID
                try {
                    uavId = Integer.parseInt(identifier);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("无效的无人机标识符: " + identifier);
                }
            }

            List<FlightTrack> tracks = dispatchService.getUavTracks(uavId, limit);

            result.put("success", true);
            result.put("data", tracks);
            result.put("count", tracks.size());

        } catch (Exception e) {
            log.error("获取无人机轨迹失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "获取轨迹失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取系统状态
     */
    @GetMapping("/system/stats")
    public Map<String, Object> getSystemStats() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("获取系统统计数据");

            LambdaQueryWrapper<Uav> uavQuery = new LambdaQueryWrapper<>();
            uavQuery.eq(Uav::getDeleted, 0);
            long totalUavs = uavMapper.selectCount(uavQuery);

            LambdaQueryWrapper<Uav> idleQuery = new LambdaQueryWrapper<>();
            idleQuery.eq(Uav::getDeleted, 0)
                    .eq(Uav::getStatus, Uav.UavStatus.IDLE);
            long idleUavs = uavMapper.selectCount(idleQuery);

            LambdaQueryWrapper<CruiseMission> missionQuery = new LambdaQueryWrapper<>();
            missionQuery.eq(CruiseMission::getStatus, CruiseMission.MissionStatus.ACTIVE)
                    .eq(CruiseMission::getDeleted, 0);
            long activeMissions = missionMapper.selectCount(missionQuery);

            LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<>();
            orderQuery.eq(Order::getStatus, Order.OrderStatus.PENDING)
                    .eq(Order::getDeleted, 0);
            long pendingOrders = orderMapper.selectCount(orderQuery);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUavs", totalUavs);
            stats.put("idleUavs", idleUavs);
            stats.put("activeMissions", activeMissions);
            stats.put("pendingOrders", pendingOrders);
            stats.put("timestamp", System.currentTimeMillis());

            result.put("success", true);
            result.put("data", stats);
            log.info("系统统计数据: {}", stats);

        } catch (Exception e) {
            log.error("获取系统统计失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "获取统计失败");
        }

        return result;
    }

    /**
     * 获取实时无人机状态（从数据库，不依赖Redis）
     */
    @GetMapping("/uavs/status/realtime")
    public Map<String, Object> getRealtimeUavStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("获取实时无人机状态");

            // 从数据库获取所有无人机的状态
            LambdaQueryWrapper<Uav> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Uav::getDeleted, 0);
            List<Uav> uavs = uavMapper.selectList(queryWrapper);

            List<Map<String, Object>> uavStatusList = uavs.stream().map(uav -> {
                Map<String, Object> status = new HashMap<>();
                status.put("id", uav.getId());
                status.put("sn", uav.getSn());
                status.put("model", uav.getModel());
                status.put("status", uav.getStatus() != null ? uav.getStatus().name() : "UNKNOWN");
                status.put("batteryLevel", uav.getBatteryLevel());
                status.put("currentLng", uav.getCurrentLng());
                status.put("currentLat", uav.getCurrentLat());
                status.put("currentMission", uav.getCurrentMission());
                status.put("loadCapacity", uav.getLoadCapacity());
                status.put("updatedTime", uav.getUpdatedTime());

                // 尝试从Redis获取最新遥测数据，如果失败则忽略
                try {
                    String telemetryStr = (String) redisTemplate.opsForValue().get("uav:status:" + uav.getSn());
                    if (telemetryStr != null) {
                        JSONObject telemetry = JSONObject.parseObject(telemetryStr);
                        status.put("realtime", telemetry);
                    }
                } catch (Exception e) {
                    log.debug("无法从Redis获取实时数据: uavSn={}", uav.getSn());
                    // 忽略Redis错误，继续使用数据库数据
                }

                return status;
            }).collect(Collectors.toList());

            result.put("success", true);
            result.put("data", uavStatusList);
            result.put("message", "获取实时状态成功");
            log.info("返回实时状态数据: {} 架无人机", uavStatusList.size());

        } catch (Exception e) {
            log.error("获取实时状态失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", STR."获取实时状态失败: \{e.getMessage()}");
        }

        return result;
    }
    /**
     * 测试接口
     */
    @GetMapping("/uavs/test")
    public Map<String, Object> getUavsTest() {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("测试获取无人机数据（无删除条件）");
            List<Uav> uavs = uavMapper.selectList(null);

            // 转换为简单Map避免序列化问题
            List<Map<String, Object>> simpleUavs = uavs.stream().map(uav -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", uav.getId());
                map.put("sn", uav.getSn());
                map.put("model", uav.getModel());
                map.put("status", uav.getStatus());
                map.put("batteryLevel", uav.getBatteryLevel());
                return map;
            }).collect(Collectors.toList());

            result.put("success", true);
            result.put("data", simpleUavs);
            result.put("count", uavs.size());
            log.info("测试查询返回无人机数据: {} 条", uavs.size());

        } catch (Exception e) {
            log.error("测试查询无人机数据失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getName());
        }

        return result;
    }

    /**
     * 更新订单状态
     */
    @PostMapping("/orders/{orderId}/status")
    public Map<String, Object> updateOrderStatus(@PathVariable Integer orderId,
                                                 @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String status = request.get("status");
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());

            Order order = orderMapper.selectById(orderId);
            if (order != null) {
                order.setStatus(orderStatus);
                orderMapper.updateById(order);

                result.put("success", true);
                result.put("message", "订单状态更新成功");
            } else {
                result.put("success", false);
                result.put("message", "订单不存在");
            }
        } catch (Exception e) {
            log.error("更新订单状态失败", e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新任务状态
     */
    @PostMapping("/missions/{missionId}/status")
    public Map<String, Object> updateMissionStatus(@PathVariable Integer missionId,
                                                   @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String status = request.get("status");
            CruiseMission.MissionStatus missionStatus = CruiseMission.MissionStatus.valueOf(status.toUpperCase());

            CruiseMission mission = missionMapper.selectById(missionId);
            if (mission != null) {
                mission.setStatus(missionStatus);
                if (missionStatus == CruiseMission.MissionStatus.COMPLETED) {
                    mission.setEndTime(LocalDateTime.now());
                }
                missionMapper.updateById(mission);

                result.put("success", true);
                result.put("message", "任务状态更新成功");
            } else {
                result.put("success", false);
                result.put("message", "任务不存在");
            }
        } catch (Exception e) {
            log.error("更新任务状态失败", e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }
    @PostMapping("/test/create-order")
    public Map<String, Object> testCreateOrder() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 测试创建订单
            Order order = dispatchService.createExpressOrder("南昌站", "南昌西站", 1.5);
            result.put("success", true);
            result.put("order", order);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        return result;
    }
    @Autowired
    private RoutePlanningService routePlanningService;

    /**
     * 增强的路线规划接口（支持无人机ID）
     */
    @PostMapping("/route/plan/enhanced")
    public Map<String, Object> enhancedPlanRoute(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("增强路线规划请求: {}", request);

            // 参数验证
            if (request == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }

            Double startLng = getDoubleValue(request.get("startLng"));
            Double startLat = getDoubleValue(request.get("startLat"));
            Double endLng = getDoubleValue(request.get("endLng"));
            Double endLat = getDoubleValue(request.get("endLat"));
            Integer uavId = null;
            if (request.get("uavId") != null) {
                uavId = Integer.valueOf(request.get("uavId").toString());
            }

            if (startLng == null || startLat == null || endLng == null || endLat == null) {
                throw new IllegalArgumentException("缺少必要的经纬度参数");
            }

            // 获取障碍物信息（可选）
            List<RoutePlanningService.Obstacle> obstacles = new ArrayList<>();

            // 执行增强路线规划
            RoutePlanningService.RoutePlanningResult planningResult =
                    routePlanningService.aStarPathPlanning(startLng, startLat, endLng, endLat, obstacles, uavId);

            if (planningResult.isSuccess()) {
                Map<String, Object> routeInfo = new HashMap<>();
                routeInfo.put("path", planningResult.getPath());
                routeInfo.put("totalDistance", Math.round(planningResult.getTotalDistance()));
                routeInfo.put("estimatedTime", Math.round(planningResult.getEstimatedTime()));
                routeInfo.put("waypointCount", planningResult.getWaypointCount());
                routeInfo.put("instructions", planningResult.getInstructions());

                result.put("success", true);
                result.put("data", routeInfo);
                result.put("message", "路线规划成功");

                log.info("增强路线规划完成: 距离{}米, 时间{}秒, {}个航点, {}条说明",
                        Math.round(planningResult.getTotalDistance()),
                        Math.round(planningResult.getEstimatedTime()),
                        planningResult.getWaypointCount(),
                        planningResult.getInstructions().size());

            } else {
                result.put("success", false);
                result.put("message", planningResult.getMessage());
            }

        } catch (Exception e) {
            log.error("增强路线规划失败", e);
            result.put("success", false);
            result.put("message", "路线规划失败: " + e.getMessage());
        }

        return result;
    }
    /**
     * 保持原有的路线规划接口兼容性（支持无人机ID）
     */
    @PostMapping("/route/plan")
    public Map<String, Object> planRoute(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("路线规划请求: {}", request);

            // 参数验证
            if (request == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }

            Double startLng = getDoubleValue(request.get("startLng"));
            Double startLat = getDoubleValue(request.get("startLat"));
            Double endLng = getDoubleValue(request.get("endLng"));
            Double endLat = getDoubleValue(request.get("endLat"));
            Integer uavId = null;
            if (request.get("uavId") != null) {
                uavId = Integer.valueOf(request.get("uavId").toString());
            }

            if (startLng == null || startLat == null || endLng == null || endLat == null) {
                throw new IllegalArgumentException("缺少必要的经纬度参数");
            }

            // 获取障碍物信息（可选）
            List<RoutePlanningService.Obstacle> obstacles = new ArrayList<>();

            // 执行路线规划
            RoutePlanningService.RoutePlanningResult planningResult =
                    routePlanningService.aStarPathPlanning(startLng, startLat, endLng, endLat, obstacles, uavId);

            if (planningResult.isSuccess()) {
                Map<String, Object> routeInfo = new HashMap<>();
                routeInfo.put("path", planningResult.getPath());
                routeInfo.put("totalDistance", Math.round(planningResult.getTotalDistance()));
                routeInfo.put("estimatedTime", Math.round(planningResult.getEstimatedTime()));
                routeInfo.put("waypointCount", planningResult.getWaypointCount());

                result.put("success", true);
                result.put("data", routeInfo);
                result.put("message", "路线规划成功");

                log.info("路线规划完成: 距离{}米, 时间{}秒, {}个航点",
                        Math.round(planningResult.getTotalDistance()),
                        Math.round(planningResult.getEstimatedTime()),
                        planningResult.getWaypointCount());

            } else {
                result.put("success", false);
                result.put("message", planningResult.getMessage());
            }

        } catch (Exception e) {
            log.error("路线规划失败", e);
            result.put("success", false);
            result.put("message", "路线规划失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 安全获取Double值
     */
    private Double getDoubleValue(Object value) {
        if (value == null) return null;
        try {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else {
                return Double.parseDouble(value.toString());
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
    /**
     * 创建医疗救援订单
     */
    @PostMapping("/orders/medical")
    public Map<String, Object> createMedicalOrder(@RequestBody MedicalOrderRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("创建医疗救援订单请求: {}", request);

            // 参数验证
            if (request == null) {
                throw new IllegalArgumentException("请求参数不能为空");
            }

            String fromLocation = request.getFromLocation();
            String toLocation = request.getToLocation();
            Double weight = request.getWeight();
            String severityLevel = request.getSeverityLevel();
            String hospitalName = request.getHospitalName();
            Long medicalDataId = request.getMedicalDataId();
            String droneSn = request.getDroneSn();  // 获取无人机序列号

            if (fromLocation == null || toLocation == null || weight == null ||
                    severityLevel == null || hospitalName == null || medicalDataId == null || droneSn == null) {
                throw new IllegalArgumentException("缺少医疗订单必要参数");
            }

            log.info("开始创建医疗订单: {} -> {}, 重量: {}kg, 严重程度: {}, 医院: {}, 医疗数据ID: {}, 无人机SN: {}",
                    fromLocation, toLocation, weight, severityLevel, hospitalName, medicalDataId, droneSn);

            // 根据无人机SN获取无人机ID
            LambdaQueryWrapper<Uav> uavQueryWrapper = new LambdaQueryWrapper<>();
            uavQueryWrapper.eq(Uav::getSn, droneSn).eq(Uav::getDeleted, 0);
            Uav selectedUav = uavMapper.selectOne(uavQueryWrapper);

            if (selectedUav == null) {
                throw new RuntimeException("未找到指定无人机: " + droneSn);
            }

            // 检查无人机状态是否可用
            if (selectedUav.getStatus() != Uav.UavStatus.IDLE && selectedUav.getStatus() != Uav.UavStatus.CHARGING) {
                throw new RuntimeException("无人机 " + droneSn + " 当前状态为 " + selectedUav.getStatus() + "，不可用");
            }

            // 1. 先创建基础订单（修改：传入指定的无人机ID）
            Order order = new Order();
            order.setOrderSn("MED_" + System.currentTimeMillis());
            order.setStatus(Order.OrderStatus.PENDING);
            order.setSystemType("MEDICAL");
            order.setSeverityLevel(severityLevel);
            order.setHospitalName(hospitalName);
            order.setMedicalDataId(medicalDataId);

            // 获取地点坐标
            Map<String, NanChangLocationConfig.Location> locations = locationConfig.getLocations();
            NanChangLocationConfig.Location from = locations.get(fromLocation);
            NanChangLocationConfig.Location to = locations.get(toLocation);

            if (from == null || to == null) {
                throw new IllegalArgumentException("地点信息不正确");
            }

            order.setPickupLng(from.getLng());
            order.setPickupLat(from.getLat());
            order.setDeliveryLng(to.getLng());
            order.setDeliveryLat(to.getLat());
            order.setWeight(weight);

            // 直接指定无人机ID，避免自动分配
            order.setAssignedUavId(selectedUav.getId());
            order.setStatus(Order.OrderStatus.ASSIGNED);  // 直接设置为已分配

            // 插入订单
            orderMapper.insert(order);
            log.info("医疗救援订单创建成功: orderId={}, orderSn={}, assignedUavId={}",
                    order.getId(), order.getOrderSn(), selectedUav.getId());

            // 2. 更新无人机状态
            selectedUav.setStatus(Uav.UavStatus.ON_MISSION);
            selectedUav.setCurrentMission("ORDER_" + order.getId());
            uavMapper.updateById(selectedUav);
            log.info("无人机状态已更新: uavId={}, uavSn={}, status=ON_MISSION",
                    selectedUav.getId(), selectedUav.getSn());

            // 3. 更新医疗数据状态
            try {
                InspectionData inspectionData = inspectionDataMapper.selectById(medicalDataId);
                if (inspectionData != null) {
                    inspectionData.setDroneId(droneSn);
                    inspectionData.setStatus(InspectionData.Status.valueOf("EXECUTING"));
                    inspectionData.setUpdatedAt(LocalDateTime.now());
                    inspectionDataMapper.updateById(inspectionData);
                    log.info("成功更新医疗数据状态: dataId={}, droneId={}", medicalDataId, droneSn);
                } else {
                    log.warn("未找到医疗数据: dataId={}", medicalDataId);
                }
            } catch (Exception e) {
                log.error("更新医疗数据状态失败: {}", e.getMessage(), e);
                // 不抛出异常，继续执行订单创建流程
            }

            result.put("success", true);
            result.put("data", order);
            result.put("message", "医疗救援订单创建成功");
            log.info("医疗救援订单创建成功: orderId={}, orderSn={}", order.getId(), order.getOrderSn());

        } catch (Exception e) {
            log.error("创建医疗救援订单失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("message", "创建医疗救援订单失败: " + e.getMessage());
        }

        return result;
    }
    // 修改 MedicalOrderRequest 类，添加 droneSn 字段
    @Data
    public static class MedicalOrderRequest {
        private String fromLocation;
        private String toLocation;
        private Double weight;
        private String severityLevel;
        private String hospitalName;
        private Long medicalDataId;
        private String droneSn;  // 无人机序列号
        private Integer assignedUavId;  // 可选：无人机ID
    }
    /**
     * 同步医疗任务状态
     */
    @PostMapping("/medical-tasks/{taskId}/sync-status")
    public Map<String, Object> syncMedicalTaskStatus(@PathVariable Long taskId,
                                                     @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("同步医疗任务状态: taskId={}, request={}", taskId, request);

            String status = request.get("status");
            String droneId = request.get("droneId");

            InspectionData inspectionData = inspectionDataMapper.selectById(taskId);
            if (inspectionData != null) {
                if (droneId != null) {
                    inspectionData.setDroneId(droneId);
                }
                // 如果有status字段可以设置
                // inspectionData.setStatus(status);
                inspectionData.setUpdatedAt(LocalDateTime.now());
                inspectionDataMapper.updateById(inspectionData);

                result.put("success", true);
                result.put("message", "医疗任务状态同步成功");
                log.info("医疗任务状态同步成功: taskId={}, droneId={}", taskId, droneId);
            } else {
                result.put("success", false);
                result.put("message", "未找到医疗任务");
            }

        } catch (Exception e) {
            log.error("同步医疗任务状态失败", e);
            result.put("success", false);
            result.put("message", "同步失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 更新巡检数据状态
     */
    @PostMapping("/inspection-data/{dataId}/status")
    public Map<String, Object> updateInspectionDataStatus(@PathVariable Long dataId,
                                                          @RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("更新巡检数据状态: dataId={}, request={}", dataId, request);

            String statusStr = request.get("status");
            String droneId = request.get("droneId");

            InspectionData inspectionData = inspectionDataMapper.selectById(dataId);
            if (inspectionData != null) {
                if (droneId != null) {
                    inspectionData.setDroneId(droneId);
                }
                if (statusStr != null) {
                    // 使用枚举的 fromString 方法或直接转换
                    try {
                        InspectionData.Status status = InspectionData.Status.valueOf(statusStr.toUpperCase());
                        inspectionData.setStatus(status);
                    } catch (IllegalArgumentException e) {
                        // 如果枚举中没有对应的值，使用默认值或自定义转换
                        log.warn("无法识别的状态值: {}, 使用默认状态PENDING", statusStr);
                        inspectionData.setStatus(InspectionData.Status.PENDING);
                    }
                }
                inspectionData.setUpdatedAt(LocalDateTime.now());
                inspectionDataMapper.updateById(inspectionData);

                result.put("success", true);
                result.put("message", "巡检数据状态更新成功");
                log.info("巡检数据状态更新成功: dataId={}, droneId={}, status={}",
                        dataId, droneId, statusStr);
            } else {
                result.put("success", false);
                result.put("message", "未找到巡检数据");
            }

        } catch (Exception e) {
            log.error("更新巡检数据状态失败", e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取电力巡检数据
     */
    @GetMapping("/inspection-data/power")
    public Map<String, Object> getPowerInspectionData() {
        Map<String, Object> result = new HashMap<>();

        try {
            LambdaQueryWrapper<InspectionData> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(InspectionData::getSystemType, "POWER")
                    .orderByDesc(InspectionData::getCaptureTime);

            List<InspectionData> inspectionDataList = inspectionDataMapper.selectList(queryWrapper);

            result.put("success", true);
            result.put("data", inspectionDataList);
            result.put("count", inspectionDataList.size());

        } catch (Exception e) {
            log.error("获取电力巡检数据失败", e);
            result.put("success", false);
            result.put("message", "获取数据失败: " + e.getMessage());
        }

        return result;
    }
}