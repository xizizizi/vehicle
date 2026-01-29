package io.vehicle.vehicle_admin.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.vehicle.vehicle_admin.config.NanChangLocationConfig;
import io.vehicle.vehicle_admin.entity.CruiseMission;
import io.vehicle.vehicle_admin.entity.FlightTrack;
import io.vehicle.vehicle_admin.entity.Order;
import io.vehicle.vehicle_admin.entity.Uav;
import io.vehicle.vehicle_admin.mapper.CruiseMissionMapper;
import io.vehicle.vehicle_admin.mapper.OrderMapper;
import io.vehicle.vehicle_admin.mapper.UavMapper;
import io.vehicle.vehicle_admin.service.DispatchService;
import io.vehicle.vehicle_admin.service.RoutePlanningService;
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
     * 增强的路线规划接口
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

            if (startLng == null || startLat == null || endLng == null || endLat == null) {
                throw new IllegalArgumentException("缺少必要的经纬度参数");
            }

            // 获取障碍物信息（可选）
            List<RoutePlanningService.Obstacle> obstacles = new ArrayList<>();

            // 执行增强路线规划
            RoutePlanningService.RoutePlanningResult planningResult =
                    routePlanningService.aStarPathPlanning(startLng, startLat, endLng, endLat, obstacles);

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
     * 保持原有的路线规划接口兼容性
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

            if (startLng == null || startLat == null || endLng == null || endLat == null) {
                throw new IllegalArgumentException("缺少必要的经纬度参数");
            }

            // 获取障碍物信息（可选）
            List<RoutePlanningService.Obstacle> obstacles = new ArrayList<>();

            // 执行路线规划
            RoutePlanningService.RoutePlanningResult planningResult =
                    routePlanningService.aStarPathPlanning(startLng, startLat, endLng, endLat, obstacles);

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


}