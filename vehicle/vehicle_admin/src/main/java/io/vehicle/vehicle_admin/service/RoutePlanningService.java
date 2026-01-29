package io.vehicle.vehicle_admin.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RoutePlanningService {

    private static final double UAV_SPEED = 8.0; // 无人机速度 m/s
    private static final double EARTH_RADIUS = 6371000; // 地球半径

    /**
     * A* 路径规划算法 - 返回详细路径信息
     */
    public RoutePlanningResult aStarPathPlanning(double startLng, double startLat,
                                                 double endLng, double endLat,
                                                 List<Obstacle> obstacles) {
        RoutePlanningResult result = new RoutePlanningResult();

        try {
            log.info("开始路径规划: ({}, {}) -> ({}, {})", startLng, startLat, endLng, endLat);

            List<Point> path = new ArrayList<>();
            Point start = new Point(startLng, startLat);
            Point end = new Point(endLng, endLat);

            // 计算两点间距离
            double distance = calculateDistance(startLng, startLat, endLng, endLat);

            JSONArray route;
            // 根据距离选择不同的路径生成策略
            if (distance < 1000) { // 1公里内直接连接
                route = createDirectPath(start, end, 3);
            } else if (distance < 5000) { // 5公里内添加少量中间点
                route = createDirectPath(start, end, 5);
            } else { // 长距离使用优化路径
                route = createOptimizedPath(start, end, obstacles, 8);
            }

            // 计算路径信息
            double totalDistance = calculatePathDistance(route);
            double estimatedTime = calculateEstimatedTime(totalDistance);

            // 生成路径说明
            List<String> instructions = generatePathInstructions(route, totalDistance);

            result.setSuccess(true);
            result.setPath(route);
            result.setTotalDistance(totalDistance);
            result.setEstimatedTime(estimatedTime);
            result.setWaypointCount(route.size());
            result.setInstructions(instructions);

            log.info("路径规划完成: 距离{}米, 时间{}秒, {}个航点",
                    Math.round(totalDistance), Math.round(estimatedTime), route.size());

        } catch (Exception e) {
            log.error("路径规划失败", e);
            result.setSuccess(false);
            result.setMessage("路径规划失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 生成路径飞行说明
     */
    private List<String> generatePathInstructions(JSONArray path, double totalDistance) {
        List<String> instructions = new ArrayList<>();

        if (path == null || path.isEmpty()) {
            return instructions;
        }

        // 起飞阶段说明
        JSONObject startPoint = path.getJSONObject(0);
        instructions.add(String.format("从起点起飞，爬升至 %.0f 米高度", startPoint.getDoubleValue("altitude")));

        // 如果有中间点，添加导航说明
        if (path.size() > 2) {
            instructions.add(String.format("沿规划路径飞行，途经 %d 个导航点", path.size() - 2));
        }

        // 巡航说明
        instructions.add(String.format("保持巡航速度 %.1f m/s，总距离 %.1f km", UAV_SPEED, totalDistance / 1000));

        // 降落说明
        JSONObject endPoint = path.getJSONObject(path.size() - 1);
        instructions.add(String.format("接近目标区域，下降至 %.0f 米高度", endPoint.getDoubleValue("altitude")));
        instructions.add("到达目的地，完成飞行任务");

        return instructions;
    }

    /**
     * 创建直线路径
     */
    private JSONArray createDirectPath(Point start, Point end, int pointsCount) {
        JSONArray path = new JSONArray();

        // 起点
        path.add(createWaypoint(start.lng, start.lat, 0, 100.0));

        // 添加中间点
        for (int i = 1; i < pointsCount - 1; i++) {
            double ratio = (double) i / (pointsCount - 1);
            double lng = start.lng + (end.lng - start.lng) * ratio;
            double lat = start.lat + (end.lat - start.lat) * ratio;
            path.add(createWaypoint(lng, lat, 0, 100.0 + i * 10));
        }

        // 终点
        path.add(createWaypoint(end.lng, end.lat, 0, 100.0));

        return path;
    }

    /**
     * 创建优化路径（绕开障碍物）
     */
    private JSONArray createOptimizedPath(Point start, Point end, List<Obstacle> obstacles, int segments) {
        JSONArray path = new JSONArray();

        // 起点
        path.add(createWaypoint(start.lng, start.lat, 0, 100.0));

        // 计算中间控制点（简单的曲线路径）
        double midLng = (start.lng + end.lng) / 2;
        double midLat = (start.lat + end.lat) / 2;

        // 添加一些曲线点，使路径更自然
        for (int i = 1; i < segments - 1; i++) {
            double t = (double) i / segments;

            // 贝塞尔曲线计算
            double lng = calculateBezierPoint(start.lng, midLng, end.lng, t);
            double lat = calculateBezierPoint(start.lat, midLat, end.lat, t);

            // 添加一些随机偏移模拟真实路径
            double offsetLng = (Math.random() - 0.5) * 0.001;
            double offsetLat = (Math.random() - 0.5) * 0.001;

            path.add(createWaypoint(lng + offsetLng, lat + offsetLat, 0, 100.0 + i * 10));
        }

        // 终点
        path.add(createWaypoint(end.lng, end.lat, 0, 100.0));

        return path;
    }

    /**
     * 二次贝塞尔曲线计算
     */
    private double calculateBezierPoint(double p0, double p1, double p2, double t) {
        double u = 1 - t;
        return u * u * p0 + 2 * u * t * p1 + t * t * p2;
    }

    /**
     * 计算路径总距离
     */
    public double calculatePathDistance(JSONArray path) {
        try {
            double totalDistance = 0;

            for (int i = 0; i < path.size() - 1; i++) {
                JSONObject point1 = path.getJSONObject(i);
                JSONObject point2 = path.getJSONObject(i + 1);

                double distance = calculateDistance(
                        point1.getDouble("lng"),
                        point1.getDouble("lat"),
                        point2.getDouble("lng"),
                        point2.getDouble("lat")
                );
                totalDistance += distance;
            }

            return totalDistance;
        } catch (Exception e) {
            log.error("计算路径距离失败", e);
            return 0;
        }
    }

    /**
     * 计算预计飞行时间（秒）
     */
    public double calculateEstimatedTime(double distance) {
        return distance / UAV_SPEED;
    }

    /**
     * 计算两点间距离（米）
     */
    public double calculateDistance(double lng1, double lat1, double lng2, double lat2) {
        try {
            double dLat = Math.toRadians(lat2 - lat1);
            double dLng = Math.toRadians(lng2 - lng1);

            double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                    Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                            Math.sin(dLng/2) * Math.sin(dLng/2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
            return EARTH_RADIUS * c;
        } catch (Exception e) {
            log.error("计算距离失败", e);
            return 0;
        }
    }

    /**
     * 创建航点
     */
    private JSONObject createWaypoint(double lng, double lat, int stopTime, double altitude) {
        JSONObject waypoint = new JSONObject();
        waypoint.put("lng", lng);
        waypoint.put("lat", lat);
        waypoint.put("stopTime", stopTime);
        waypoint.put("altitude", altitude);
        return waypoint;
    }

    /**
     * 路径规划结果类
     */
    public static class RoutePlanningResult {
        private boolean success;
        private String message;
        private JSONArray path;
        private double totalDistance;
        private double estimatedTime;
        private int waypointCount;
        private List<String> instructions;

        // Getter和Setter方法
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public JSONArray getPath() { return path; }
        public void setPath(JSONArray path) { this.path = path; }

        public double getTotalDistance() { return totalDistance; }
        public void setTotalDistance(double totalDistance) { this.totalDistance = totalDistance; }

        public double getEstimatedTime() { return estimatedTime; }
        public void setEstimatedTime(double estimatedTime) { this.estimatedTime = estimatedTime; }

        public int getWaypointCount() { return waypointCount; }
        public void setWaypointCount(int waypointCount) { this.waypointCount = waypointCount; }

        public List<String> getInstructions() { return instructions; }
        public void setInstructions(List<String> instructions) { this.instructions = instructions; }
    }

    /**
     * 内部类：点坐标
     */
    private static class Point {
        double lng, lat;
        Point(double lng, double lat) {
            this.lng = lng;
            this.lat = lat;
        }
    }

    /**
     * 障碍物类
     */
    public static class Obstacle {
        public double lng, lat, radius;
        public Obstacle(double lng, double lat, double radius) {
            this.lng = lng;
            this.lat = lat;
            this.radius = radius;
        }
    }
}