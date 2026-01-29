package io.vehicle.vehicle_admin.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.vehicle.vehicle_admin.config.NanChangLocationConfig;
import io.vehicle.vehicle_admin.entity.*;
import io.vehicle.vehicle_admin.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class DispatchService {

    @Autowired
    @Qualifier("mqttOutboundChannel")
    private MessageChannel mqttOutboundChannel;

    @Autowired
    private UavMapper uavMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CruiseMissionMapper missionMapper;

    @Autowired
    private FlightTrackMapper trackMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private NanChangLocationConfig locationConfig;

    private Random random = new Random();

    /**
     * 处理无人机遥测数据
     */
    @Transactional
    public void handleUavTelemetry(String uavSn, JSONObject telemetry) {
        try {
            log.debug("处理无人机遥测数据: uavSn={}, telemetry={}", uavSn, telemetry);

            // 更新Redis缓存
            try {
                redisTemplate.opsForValue().set(
                        "uav:status:" + uavSn,
                        telemetry.toString(),
                        Duration.ofMinutes(5)
                );
            } catch (Exception e) {
                log.warn("Redis缓存更新失败: uavSn={}", uavSn, e);
            }

            // 更新数据库
            LambdaQueryWrapper<Uav> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Uav::getSn, uavSn).eq(Uav::getDeleted, 0);
            Uav uav = uavMapper.selectOne(queryWrapper);

            if (uav != null) {
                updateUavFromTelemetry(uav, telemetry);
                uavMapper.updateById(uav);

                // 记录飞行轨迹
                recordFlightTrack(uav, telemetry);
            } else {
                log.warn("未找到无人机: uavSn={}", uavSn);
            }

        } catch (Exception e) {
            log.error("处理无人机遥测数据失败: uavSn={}", uavSn, e);
            throw new RuntimeException("处理遥测数据失败", e);
        }
    }

    /**
     * 从遥测数据更新无人机状态
     */
    private void updateUavFromTelemetry(Uav uav, JSONObject telemetry) {
        if (telemetry.getDouble("lng") != null) {
            uav.setCurrentLng(telemetry.getDouble("lng"));
        }
        if (telemetry.getDouble("lat") != null) {
            uav.setCurrentLat(telemetry.getDouble("lat"));
        }
        if (telemetry.getInteger("battery") != null) {
            uav.setBatteryLevel(telemetry.getInteger("battery"));
        }
        if (telemetry.getString("currentMission") != null) {
            uav.setCurrentMission(telemetry.getString("currentMission"));
        }

        // 将字符串状态转换为枚举
        String statusStr = telemetry.getString("status");
        if (statusStr != null) {
            try {
                Uav.UavStatus status = Uav.UavStatus.valueOf(statusStr.toUpperCase());
                uav.setStatus(status);
            } catch (IllegalArgumentException e) {
                log.warn("无效的无人机状态: {}, 使用IDLE作为默认值", statusStr);
                uav.setStatus(Uav.UavStatus.IDLE);
            }
        }

        uav.setUpdatedTime(LocalDateTime.now());
    }

    /**
     * 记录飞行轨迹
     */
    private void recordFlightTrack(Uav uav, JSONObject telemetry) {
        try {
            FlightTrack track = new FlightTrack();
            track.setUavId(uav.getId());
            track.setLng(telemetry.getDouble("lng"));
            track.setLat(telemetry.getDouble("lat"));
            track.setAltitude(telemetry.getDouble("altitude"));
            track.setSpeed(telemetry.getDouble("speed"));
            track.setBattery(telemetry.getInteger("battery"));
            track.setRecordedTime(LocalDateTime.now());

            if (uav.getCurrentMission() != null && uav.getCurrentMission().startsWith("MISSION_")) {
                String missionIdStr = uav.getCurrentMission().replace("MISSION_", "");
                try {
                    track.setMissionId(Integer.parseInt(missionIdStr));
                } catch (NumberFormatException ex) {
                    log.warn("任务ID格式无效: {}", uav.getCurrentMission());
                }
            }

            trackMapper.insert(track);
        } catch (Exception e) {
            log.error("记录飞行轨迹失败", e);
        }
    }

    /**
     * 创建快递订单 - 修复地点配置问题
     */
    @Transactional
    public Order createExpressOrder(String fromLocation, String toLocation, Double weight) {
        try {
            log.info("创建快递订单: from={}, to={}, weight={}", fromLocation, toLocation, weight);

            // 获取地点配置
            Map<String, NanChangLocationConfig.Location> locations = locationConfig.getLocations();
            log.info("可用地点数量: {}", locations.size());
            log.info("可用地点: {}", locations.keySet());

            if (locations == null || locations.isEmpty()) {
                // 如果配置为空，使用硬编码的默认值
                log.warn("地点配置为空，使用硬编码默认值");
                locations = createHardcodedLocations();
            }

            NanChangLocationConfig.Location from = locations.get(fromLocation);
            NanChangLocationConfig.Location to = locations.get(toLocation);

            if (from == null) {
                throw new IllegalArgumentException("起始地点 '" + fromLocation + "' 不存在。可用地点: " + locations.keySet());
            }
            if (to == null) {
                throw new IllegalArgumentException("目的地点 '" + toLocation + "' 不存在。可用地点: " + locations.keySet());
            }

            log.info("起始地点坐标: {}({}, {})", fromLocation, from.getLng(), from.getLat());
            log.info("目的地点坐标: {}({}, {})", toLocation, to.getLng(), to.getLat());

            // 创建订单对象
            Order order = new Order();
            order.setOrderSn("EXP_" + System.currentTimeMillis());
            order.setStatus(Order.OrderStatus.PENDING);
            order.setPickupLng(from.getLng());
            order.setPickupLat(from.getLat());
            order.setDeliveryLng(to.getLng());
            order.setDeliveryLat(to.getLat());
            order.setWeight(weight);

            log.info("准备插入订单: orderSn={}", order.getOrderSn());

            // 插入订单
            int insertResult = orderMapper.insert(order);
            log.info("订单插入结果: {}, 生成的订单ID: {}", insertResult, order.getId());

            if (order.getId() == null) {
                throw new RuntimeException("订单插入失败，未生成ID");
            }

            log.info("订单创建成功: orderId={}, orderSn={}", order.getId(), order.getOrderSn());

            // 尝试调度订单（异步）
            try {
                dispatchOrder(order);
            } catch (Exception e) {
                log.warn("订单调度失败，但订单已创建成功: orderId={}", order.getId(), e);
                // 不抛出异常，订单创建成功
            }

            return order;

        } catch (Exception e) {
            log.error("创建快递订单失败: from={}, to={}, weight={}", fromLocation, toLocation, weight, e);
            throw new RuntimeException("创建订单失败: " + e.getMessage(), e);
        }
    }

    /**
     * 硬编码的默认地点（确保系统能正常工作）
     */
    private Map<String, NanChangLocationConfig.Location> createHardcodedLocations() {
        Map<String, NanChangLocationConfig.Location> locations = new HashMap<>();
        locations.put("南昌站", new NanChangLocationConfig.Location(115.907, 28.662));
        locations.put("南昌西站", new NanChangLocationConfig.Location(115.768, 28.620));
        locations.put("昌北机场", new NanChangLocationConfig.Location(115.900, 28.865));
        locations.put("南昌东站", new NanChangLocationConfig.Location(115.983, 28.625));
        locations.put("徐坊客运站", new NanChangLocationConfig.Location(115.889, 28.663));
        locations.put("八一广场", new NanChangLocationConfig.Location(115.899, 28.679));
        return locations;
    }
    /**
     * 调度订单
     */
    private void dispatchOrder(Order order) {
        try {
            log.info("开始调度订单: orderId={}", order.getId());

            List<Uav> availableUavs = uavMapper.selectIdleUavsWithGoodBattery(30);
            log.info("可用无人机数量: {}", availableUavs.size());

            if (availableUavs.isEmpty()) {
                log.warn("没有可用的无人机，订单保持待分配状态: orderId={}", order.getId());
                return;
            }

            Uav bestUav = null;
            double minDistance = Double.MAX_VALUE;

            for (Uav uav : availableUavs) {
                if (uav.getCurrentLng() == null || uav.getCurrentLat() == null) {
                    log.warn("无人机 {} 位置信息不完整，跳过", uav.getSn());
                    continue;
                }

                double distance = calculateDistance(
                        uav.getCurrentLng(), uav.getCurrentLat(),
                        order.getPickupLng(), order.getPickupLat()
                );

                log.debug("无人机 {} 距离: {} 米, 载重: {}kg", uav.getSn(), distance, uav.getLoadCapacity());

                if (distance < minDistance && uav.getLoadCapacity() >= order.getWeight()) {
                    minDistance = distance;
                    bestUav = uav;
                }
            }

            if (bestUav != null) {
                log.info("找到最佳无人机: {}，距离: {} 米", bestUav.getSn(), minDistance);
                assignOrderToUav(order, bestUav);
            } else {
                log.warn("没有找到合适的无人机来分配订单: orderId={}", order.getId());
                // 保持订单状态为PENDING，等待后续调度
            }
        } catch (Exception e) {
            log.error("调度订单失败: orderId={}", order.getId(), e);
            // 不抛出异常，避免影响订单创建
        }
    }

    /**
     * 分配订单给无人机
     */
    private void assignOrderToUav(Order order, Uav uav) {
        try {
            order.setStatus(Order.OrderStatus.ASSIGNED);
            order.setAssignedUavId(uav.getId());
            orderMapper.updateById(order);

            uav.setStatus(Uav.UavStatus.ON_MISSION);
            uav.setCurrentMission("ORDER_" + order.getId());
            uavMapper.updateById(uav);

            // 发送配送指令
            sendDeliveryCommand(uav, order);

            log.info("订单分配成功: orderId={} -> uavId={}", order.getId(), uav.getId());
        } catch (Exception e) {
            log.error("分配订单给无人机失败", e);
            throw new RuntimeException("分配订单失败", e);
        }
    }

    /**
     * 发送配送指令
     */
    private void sendDeliveryCommand(Uav uav, Order order) {
        try {
            JSONObject command = new JSONObject();
            command.put("command", "START_DELIVERY");
            command.put("orderId", order.getId());
            command.put("missionId", "ORDER_" + order.getId());
            command.put("pickup", new JSONObject()
                    .fluentPut("lng", order.getPickupLng())
                    .fluentPut("lat", order.getPickupLat()));
            command.put("delivery", new JSONObject()
                    .fluentPut("lng", order.getDeliveryLng())
                    .fluentPut("lat", order.getDeliveryLat()));
            command.put("speed", 10.0);
            command.put("altitude", 100.0);

            mqttOutboundChannel.send(MessageBuilder.withPayload(command.toString())
                    .setHeader(MqttHeaders.TOPIC, "uav/command/" + uav.getSn())
                    .build());

            log.info("配送指令发送成功: uavSn={}, orderId={}", uav.getSn(), order.getId());
        } catch (Exception e) {
            log.error("发送配送指令失败: uavSn={}, orderId={}", uav.getSn(), order.getId(), e);
        }
    }

    /**
     * 创建巡航任务
     */
    @Transactional
    public CruiseMission createCruiseMission(String missionName, JSONArray routePoints) {
        try {
            log.info("创建巡航任务: missionName={}, routePointsCount={}", missionName, routePoints.size());

            CruiseMission mission = new CruiseMission();
            mission.setMissionName(missionName);
            mission.setMissionType(CruiseMission.MissionType.CRUISE);
            mission.setStatus(CruiseMission.MissionStatus.PENDING);
            mission.setRoutePoints(routePoints.toJSONString()); // 使用toJSONString确保正确序列化
            mission.setPriority(2);

            missionMapper.insert(mission);
            log.info("巡航任务创建成功: missionId={}", mission.getId());

            return mission;
        } catch (Exception e) {
            log.error("创建巡航任务失败: missionName={}", missionName, e);
            throw new RuntimeException("创建巡航任务失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分配巡航任务
     */
    @Transactional
    public boolean assignCruiseMission(Integer missionId, String uavSn) {
        try {
            log.info("分配巡航任务: missionId={}, uavSn={}", missionId, uavSn);

            CruiseMission mission = missionMapper.selectById(missionId);
            LambdaQueryWrapper<Uav> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Uav::getSn, uavSn).eq(Uav::getDeleted, 0);
            Uav uav = uavMapper.selectOne(queryWrapper);

            if (mission == null) {
                log.error("任务不存在: missionId={}", missionId);
                return false;
            }
            if (uav == null) {
                log.error("无人机不存在: uavSn={}", uavSn);
                return false;
            }
            if (uav.getStatus() != Uav.UavStatus.IDLE) {
                log.error("无人机不在空闲状态: uavSn={}, status={}", uavSn, uav.getStatus());
                return false;
            }
            if (uav.getBatteryLevel() <= 40) {
                log.error("无人机电量不足: uavSn={}, battery={}", uavSn, uav.getBatteryLevel());
                return false;
            }

            mission.setAssignedUavId(uav.getId());
            mission.setStatus(CruiseMission.MissionStatus.ACTIVE);
            mission.setStartTime(LocalDateTime.now());
            missionMapper.updateById(mission);

            uav.setStatus(Uav.UavStatus.ON_MISSION);
            uav.setCurrentMission("MISSION_" + mission.getId());
            uavMapper.updateById(uav);

            sendCruiseCommand(uav, mission);
            log.info("巡航任务分配成功: missionId={} -> uavSn={}", missionId, uavSn);
            return true;

        } catch (Exception e) {
            log.error("分配巡航任务失败: missionId={}, uavSn={}", missionId, uavSn, e);
            throw new RuntimeException("分配巡航任务失败", e);
        }
    }

    /**
     * 发送巡航指令
     */
    private void sendCruiseCommand(Uav uav, CruiseMission mission) {
        try {
            JSONObject command = new JSONObject();
            command.put("command", "START_CRUISE");
            command.put("missionId", mission.getId());
            command.put("missionName", mission.getMissionName());
            command.put("route", JSONArray.parse(mission.getRoutePoints()));
            command.put("cruiseSpeed", 8.0);
            command.put("altitude", 120.0);

            mqttOutboundChannel.send(MessageBuilder.withPayload(command.toString())
                    .setHeader(MqttHeaders.TOPIC, "uav/command/" + uav.getSn())
                    .build());

            log.info("巡航指令发送成功: uavSn={}, missionId={}", uav.getSn(), mission.getId());
        } catch (Exception e) {
            log.error("发送巡航指令失败: uavSn={}, missionId={}", uav.getSn(), mission.getId(), e);
        }
    }

    /**
     * 获取无人机轨迹
     */
    public List<FlightTrack> getUavTracks(Integer uavId, Integer limit) {
        try {
            return trackMapper.selectRecentTracks(uavId, Optional.ofNullable(limit).orElse(100));
        } catch (Exception e) {
            log.error("获取无人机轨迹失败: uavId={}, limit={}", uavId, limit, e);
            throw new RuntimeException("获取轨迹失败", e);
        }
    }

    /**
     * 计算两点间距离（简化版）
     */
    private double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        double earthRadius = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }

    /**
     * 完成订单配送
     */
    @Transactional
    public void completeOrder(Integer orderId) {
        try {
            Order order = orderMapper.selectById(orderId);
            if (order != null && order.getStatus() == Order.OrderStatus.DELIVERING) {
                order.setStatus(Order.OrderStatus.COMPLETED);
                orderMapper.updateById(order);

                // 释放无人机
                if (order.getAssignedUavId() != null) {
                    Uav uav = uavMapper.selectById(order.getAssignedUavId());
                    if (uav != null) {
                        uav.setStatus(Uav.UavStatus.IDLE);
                        uav.setCurrentMission(null);
                        uavMapper.updateById(uav);
                        log.info("订单完成，无人机释放: orderId={}, uavId={}", orderId, uav.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("完成订单失败: orderId={}", orderId, e);
            throw new RuntimeException("完成订单失败", e);
        }
    }

    /**
     * 完成巡航任务
     */
    @Transactional
    public void completeMission(Integer missionId) {
        try {
            CruiseMission mission = missionMapper.selectById(missionId);
            if (mission != null && mission.getStatus() == CruiseMission.MissionStatus.ACTIVE) {
                mission.setStatus(CruiseMission.MissionStatus.COMPLETED);
                mission.setEndTime(LocalDateTime.now());
                missionMapper.updateById(mission);

                // 释放无人机
                if (mission.getAssignedUavId() != null) {
                    Uav uav = uavMapper.selectById(mission.getAssignedUavId());
                    if (uav != null) {
                        uav.setStatus(Uav.UavStatus.IDLE);
                        uav.setCurrentMission(null);
                        uavMapper.updateById(uav);
                        log.info("任务完成，无人机释放: missionId={}, uavId={}", missionId, uav.getId());
                    }
                }
            }
        } catch (Exception e) {
            log.error("完成巡航任务失败: missionId={}", missionId, e);
            throw new RuntimeException("完成巡航任务失败", e);
        }
    }

    /**
     * 路线规划服务
     */
    @Service
    public class RoutePlanningService {

        @Autowired
        private NanChangLocationConfig locationConfig;

        private static final double UAV_SPEED = 8.0; // 无人机速度 m/s
        private static final double EARTH_RADIUS = 6371000; // 地球半径

        /**
         * A* 路径规划算法
         */
        public JSONArray aStarPathPlanning(double startLng, double startLat,
                                           double endLng, double endLat,
                                           List<Obstacle> obstacles) {
            List<Point> path = new ArrayList<>();

            // 简化版 A* 算法 - 实际项目中可以使用更复杂的实现
            Point start = new Point(startLng, startLat);
            Point end = new Point(endLng, endLat);

            // 如果两点距离较近，直接返回直线路径
            double distance = calculateDistance(startLng, startLat, endLng, endLat);
            if (distance < 5000) { // 5公里内直接连接
                return createDirectPath(start, end);
            }

            // 否则添加中间航点避免障碍物
            return createOptimizedPath(start, end, obstacles);
        }

        /**
         * 创建直线路径
         */
        private JSONArray createDirectPath(Point start, Point end) {
            JSONArray path = new JSONArray();

            // 起点
            path.add(createWaypoint(start.lng, start.lat, 0));

            // 如果距离较远，添加中间点
            double distance = calculateDistance(start.lng, start.lat, end.lng, end.lat);
            if (distance > 1000) {
                int intermediatePoints = (int) (distance / 1000); // 每公里一个中间点
                for (int i = 1; i <= intermediatePoints; i++) {
                    double ratio = (double) i / (intermediatePoints + 1);
                    double lng = start.lng + (end.lng - start.lng) * ratio;
                    double lat = start.lat + (end.lat - start.lat) * ratio;
                    path.add(createWaypoint(lng, lat, 0));
                }
            }

            // 终点
            path.add(createWaypoint(end.lng, end.lat, 0));

            return path;
        }

        /**
         * 创建优化路径（绕开障碍物）
         */
        private JSONArray createOptimizedPath(Point start, Point end, List<Obstacle> obstacles) {
            JSONArray path = new JSONArray();

            // 起点
            path.add(createWaypoint(start.lng, start.lat, 0));

            // 计算中间控制点（贝塞尔曲线控制点）
            Point controlPoint1 = calculateControlPoint(start, end, 0.3);
            Point controlPoint2 = calculateControlPoint(start, end, 0.7);

            // 生成贝塞尔曲线路径点
            int segments = 10;
            for (int i = 1; i < segments; i++) {
                double t = (double) i / segments;
                Point point = calculateBezierPoint(start, controlPoint1, controlPoint2, end, t);

                // 检查是否与障碍物冲突
                if (!isPointInObstacle(point, obstacles)) {
                    path.add(createWaypoint(point.lng, point.lat, 0));
                }
            }

            // 终点
            path.add(createWaypoint(end.lng, end.lat, 0));

            return path;
        }

        /**
         * 计算贝塞尔曲线点
         */
        private Point calculateBezierPoint(Point p0, Point p1, Point p2, Point p3, double t) {
            double u = 1 - t;
            double tt = t * t;
            double uu = u * u;
            double uuu = uu * u;
            double ttt = tt * t;

            double lng = uuu * p0.lng +
                    3 * uu * t * p1.lng +
                    3 * u * tt * p2.lng +
                    ttt * p3.lng;

            double lat = uuu * p0.lat +
                    3 * uu * t * p1.lat +
                    3 * u * tt * p2.lat +
                    ttt * p3.lat;

            return new Point(lng, lat);
        }

        /**
         * 计算控制点
         */
        private Point calculateControlPoint(Point start, Point end, double ratio) {
            double midLng = (start.lng + end.lng) / 2;
            double midLat = (start.lat + end.lat) / 2;

            // 垂直方向偏移
            double dx = end.lng - start.lng;
            double dy = end.lat - start.lat;

            // 计算垂直向量
            double perpLng = -dy * 0.0001 * ratio;
            double perpLat = dx * 0.0001 * ratio;

            return new Point(midLng + perpLng, midLat + perpLat);
        }

        /**
         * 检查点是否在障碍物内
         */
        private boolean isPointInObstacle(Point point, List<Obstacle> obstacles) {
            if (obstacles == null) return false;

            for (Obstacle obstacle : obstacles) {
                double distance = calculateDistance(point.lng, point.lat,
                        obstacle.lng, obstacle.lat);
                if (distance < obstacle.radius) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 计算路径总距离
         */
        public double calculatePathDistance(JSONArray path) {
            double totalDistance = 0;

            for (int i = 0; i < path.size() - 1; i++) {
                JSONObject point1 = path.getJSONObject(i);
                JSONObject point2 = path.getJSONObject(i + 1);

                double distance = calculateDistance(
                        point1.getDouble("lng"), point1.getDouble("lat"),
                        point2.getDouble("lng"), point2.getDouble("lat")
                );
                totalDistance += distance;
            }

            return totalDistance;
        }

        /**
         * 计算预计飞行时间
         */
        public double calculateEstimatedTime(double distance) {
            return distance / UAV_SPEED; // 秒
        }

        /**
         * 创建航点
         */
        private JSONObject createWaypoint(double lng, double lat, int stopTime) {
            JSONObject waypoint = new JSONObject();
            waypoint.put("lng", lng);
            waypoint.put("lat", lat);
            waypoint.put("stopTime", stopTime);
            waypoint.put("altitude", 100.0); // 默认高度
            return waypoint;
        }

        /**
         * 计算两点间距离
         */
        private double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);

            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.sin(dLng/2) * Math.sin(dLng/2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            return EARTH_RADIUS * c;
        }

        // 内部类定义
        private static class Point {
            double lng, lat;
            Point(double lng, double lat) {
                this.lng = lng;
                this.lat = lat;
            }
        }

        public static class Obstacle {
            double lng, lat, radius;
            public Obstacle(double lng, double lat, double radius) {
                this.lng = lng;
                this.lat = lat;
                this.radius = radius;
            }
        }
    }


}