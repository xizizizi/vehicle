package io.vehicle.vehicle_admin.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.vehicle.vehicle_admin.entity.InspectionData;
import io.vehicle.vehicle_admin.entity.Uav;
import io.vehicle.vehicle_admin.mapper.InspectionDataMapper;
import io.vehicle.vehicle_admin.mapper.UavMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoutePlanningService {

    private static final double UAV_SPEED = 8.0; // 无人机速度 m/s
    private static final double EARTH_RADIUS = 6371000; // 地球半径
    private static final double GRID_SIZE = 0.01; // 网格大小（度），约1000米
    private static final int MAX_ITERATIONS = 50;   // 蚁群最大迭代次数
    private static final int ANT_COUNT = 20;        // 每代蚂蚁数量
    private static final double ALPHA = 1.0;        // 信息素重要性因子
    private static final double BETA = 2.0;          // 启发式信息重要性因子
    private static final double RHO = 0.1;           // 信息素挥发系数
    private static final double Q = 100.0;           // 信息素增量常数
    private static final double RISK_RADIUS = 0.005; // 风险区域影响半径（度），约500米
    private static final double PENALTY_OVER_RANGE = 1e6; // 超出航程惩罚
    private static final double RISK_FACTOR = 0.7; // 风险影响因子，降低后使路径更倾向直线
    @Autowired
    private UavMapper uavMapper;

    @Autowired
    private InspectionDataMapper inspectionDataMapper;

    /**
     * 路径规划算法（蚁群算法）
     *
     * @param startLng  起点经度
     * @param startLat  起点纬度
     * @param endLng    终点经度
     * @param endLat    终点纬度
     * @param obstacles 障碍物列表（暂未使用）
     * @param uavId     所选无人机的ID
     */
    public RoutePlanningResult aStarPathPlanning(double startLng, double startLat,
                                                 double endLng, double endLat,
                                                 List<Obstacle> obstacles,
                                                 Integer uavId) {
        RoutePlanningResult result = new RoutePlanningResult();

        try {
            log.info("开始路径规划: ({}, {}) -> ({}, {}), 无人机ID: {}", startLng, startLat, endLng, endLat, uavId);

            // 1. 获取无人机及其电池健康信息
            Uav selectedUav = null;
            if (uavId != null) {
                selectedUav = uavMapper.selectById(uavId);
                if (selectedUav == null) {
                    log.warn("未找到指定无人机ID: {}，将使用代表无人机", uavId);
                }
            }
            if (selectedUav == null) {
                selectedUav = getRepresentativeUav();
            }
            double maxRange = calculateMaxRange(selectedUav); // 最大航程（米）

            // 2. 获取高风险区域（从巡检数据中提取）
            List<RiskPoint> riskPoints = loadRiskPoints();

            // 3. 构建网格并执行蚁群算法
            List<Point> gridPath = antColonySearch(
                    new Point(startLng, startLat),
                    new Point(endLng, endLat),
                    riskPoints,
                    maxRange
            );

            // 4. 将网格路径转换为经纬度路径点，并添加高度信息
            JSONArray route = convertPathToWaypoints(gridPath);

            // 5. 计算路径信息
            double totalDistance = calculatePathDistance(route);
            double estimatedTime = calculateEstimatedTime(totalDistance);
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

    // ---------- 辅助方法 ----------

    /** 获取代表性无人机（用于电池约束） */
    private Uav getRepresentativeUav() {
        // 优先选择空闲无人机
        List<Uav> idleUavs = uavMapper.selectList(
                new LambdaQueryWrapper<Uav>()
                        .eq(Uav::getStatus, Uav.UavStatus.IDLE)
                        .eq(Uav::getDeleted, 0)
        );
        if (!idleUavs.isEmpty()) {
            return idleUavs.get(0);
        }
        // 若无空闲，取任意一个
        return uavMapper.selectList(
                new LambdaQueryWrapper<Uav>()
                        .eq(Uav::getDeleted, 0)
                        .last("LIMIT 1")
        ).get(0);
    }

    /** 根据无人机计算最大航程（米） */
    private double calculateMaxRange(Uav uav) {
        if (uav == null) {
            // 默认航程：30分钟 * 速度
            return 30 * 60 * UAV_SPEED;
        }
        // 假设无人机满电续航为30分钟，速度8m/s，则基础航程14400米
        double baseRange = 30 * 60 * UAV_SPEED;
        // 考虑电池SOH（健康度），SOH低于1时航程按比例缩减
        double soh = uav.getBatteryLevel() / 100.0; // 假设电量百分比近似SOH
        return baseRange * soh;
    }

    /** 从巡检数据中加载风险点 */
    private List<RiskPoint> loadRiskPoints() {
        List<InspectionData> highRiskData = inspectionDataMapper.selectList(
                new LambdaQueryWrapper<InspectionData>()
                        .in(InspectionData::getSeverityLevel, "HIGH", "CRITICAL")
                        .isNotNull(InspectionData::getLatitude)
                        .isNotNull(InspectionData::getLongitude)
        );
        return highRiskData.stream()
                .map(d -> new RiskPoint(d.getLongitude(), d.getLatitude(),
                        d.getSeverityLevel().equals("CRITICAL") ? 1.0 : 0.5))
                .collect(Collectors.toList());
    }

    /** 蚁群路径搜索 */
    private List<Point> antColonySearch(Point start, Point end, List<RiskPoint> risks, double maxRange) {
        // 如果没有风险点，直接返回直线路径
        if (risks.isEmpty()) {
            log.info("无风险区域，使用直线路径");
            return Arrays.asList(start, end);
        }

        // 确定搜索边界（起点终点外扩一定范围）
        double minLng = Math.min(start.lng, end.lng) - 0.1;
        double maxLng = Math.max(start.lng, end.lng) + 0.1;
        double minLat = Math.min(start.lat, end.lat) - 0.1;
        double maxLat = Math.max(start.lat, end.lat) + 0.1;

        // 网格划分
        int cols = (int) Math.ceil((maxLng - minLng) / GRID_SIZE) + 1;
        int rows = (int) Math.ceil((maxLat - minLat) / GRID_SIZE) + 1;

        // 将起点终点映射到网格索引
        int startCol = (int) ((start.lng - minLng) / GRID_SIZE);
        int startRow = (int) ((start.lat - minLat) / GRID_SIZE);
        int endCol = (int) ((end.lng - minLng) / GRID_SIZE);
        int endRow = (int) ((end.lat - minLat) / GRID_SIZE);

        // 初始化信息素矩阵
        double[][] pheromone = new double[rows][cols];
        for (int i = 0; i < rows; i++) Arrays.fill(pheromone[i], 1.0);

        // 预计算每个网格的风险值（距离风险点的倒数，越大越危险）
        double[][] riskMap = new double[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double lng = minLng + c * GRID_SIZE;
                double lat = minLat + r * GRID_SIZE;
                double totalRisk = 0.0;
                for (RiskPoint rp : risks) {
                    double dist = distance(lng, lat, rp.lng, rp.lat);
                    if (dist < RISK_RADIUS * 111000) { // 约500米
                        double factor = 1.0 - dist / (RISK_RADIUS * 111000);
                        totalRisk += rp.weight * factor * factor;
                    }
                }
                riskMap[r][c] = totalRisk;
            }
        }

        // 启发式信息：距离目标越近越好，风险越小越好
        double[][] heuristic = new double[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double lng = minLng + c * GRID_SIZE;
                double lat = minLat + r * GRID_SIZE;
                double distToEnd = distance(lng, lat, end.lng, end.lat);
                heuristic[r][c] = 1.0 / (distToEnd + 1) * (1.0 / (1 + RISK_FACTOR * riskMap[r][c]));
            }
        }

        // 记录最优路径
        List<Point> bestPath = null;
        double bestCost = Double.MAX_VALUE;

        // 蚁群迭代
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            List<List<int[]>> antPaths = new ArrayList<>();
            List<Double> antCosts = new ArrayList<>();

            for (int ant = 0; ant < ANT_COUNT; ant++) {
                // 每只蚂蚁构建一条路径
                List<int[]> path = new ArrayList<>();
                boolean[][] visited = new boolean[rows][cols];
                int curRow = startRow, curCol = startCol;
                path.add(new int[]{curRow, curCol});
                visited[curRow][curCol] = true;

                while (!(curRow == endRow && curCol == endCol)) {
                    // 获取可行邻居（八连通）
                    List<int[]> neighbors = new ArrayList<>();
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            int nr = curRow + dr;
                            int nc = curCol + dc;
                            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc]) {
                                neighbors.add(new int[]{nr, nc});
                            }
                        }
                    }
                    if (neighbors.isEmpty()) break; // 无路可走

                    // 计算选择概率
                    double total = 0.0;
                    double[] probs = new double[neighbors.size()];
                    for (int i = 0; i < neighbors.size(); i++) {
                        int[] nb = neighbors.get(i);
                        double tau = Math.pow(pheromone[nb[0]][nb[1]], ALPHA);
                        double eta = Math.pow(heuristic[nb[0]][nb[1]], BETA);
                        probs[i] = tau * eta;
                        total += probs[i];
                    }
                    if (total == 0) break;

                    // 轮盘赌选择
                    double rand = Math.random() * total;
                    int idx = 0;
                    double cum = probs[0];
                    while (cum < rand && idx < neighbors.size() - 1) {
                        idx++;
                        cum += probs[idx];
                    }
                    int[] chosen = neighbors.get(idx);
                    path.add(chosen);
                    visited[chosen[0]][chosen[1]] = true;
                    curRow = chosen[0];
                    curCol = chosen[1];
                }

                // 若蚂蚁到达终点，计算路径成本
                if (curRow == endRow && curCol == endCol) {
                    double cost = 0.0;
                    double distanceSum = 0.0;
                    for (int i = 0; i < path.size() - 1; i++) {
                        int[] p1 = path.get(i);
                        int[] p2 = path.get(i + 1);
                        double lng1 = minLng + p1[1] * GRID_SIZE;
                        double lat1 = minLat + p1[0] * GRID_SIZE;
                        double lng2 = minLng + p2[1] * GRID_SIZE;
                        double lat2 = minLat + p2[0] * GRID_SIZE;
                        double segDist = distance(lng1, lat1, lng2, lat2);
                        distanceSum += segDist;
                        // 成本 = 距离 * (1 + 风险)
                        double risk = riskMap[p2[0]][p2[1]];
                        cost += segDist * (1 + RISK_FACTOR * risk);
                    }
                    // 超出最大航程惩罚
                    if (distanceSum > maxRange) {
                        cost += PENALTY_OVER_RANGE;
                    }
                    antPaths.add(path);
                    antCosts.add(cost);

                    if (cost < bestCost) {
                        bestCost = cost;
                        bestPath = new ArrayList<>();
                        for (int[] p : path) {
                            bestPath.add(new Point(minLng + p[1] * GRID_SIZE, minLat + p[0] * GRID_SIZE));
                        }
                    }
                }
            }

            // 信息素挥发
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    pheromone[r][c] *= (1 - RHO);
                }
            }

            // 根据本次迭代的最优路径增加信息素
            if (!antPaths.isEmpty()) {
                // 找到本次迭代的最优
                double minIterCost = Double.MAX_VALUE;
                List<int[]> bestIterPath = null;
                for (int i = 0; i < antPaths.size(); i++) {
                    if (antCosts.get(i) < minIterCost) {
                        minIterCost = antCosts.get(i);
                        bestIterPath = antPaths.get(i);
                    }
                }
                if (bestIterPath != null) {
                    double delta = Q / minIterCost;
                    for (int[] p : bestIterPath) {
                        pheromone[p[0]][p[1]] += delta;
                    }
                }
            }
        }

        // 若未找到任何路径，返回直线路径
        if (bestPath == null) {
            log.warn("蚁群未找到可行路径，使用直线回退方案");
            bestPath = Arrays.asList(start, end);
        }
        return bestPath;
    }

    /** 将网格路径转换为航点（包含高度） */
    private JSONArray convertPathToWaypoints(List<Point> path) {
        JSONArray waypoints = new JSONArray();
        double baseAlt = 100.0;
        for (int i = 0; i < path.size(); i++) {
            Point p = path.get(i);
            JSONObject wp = new JSONObject();
            wp.put("lng", p.lng);
            wp.put("lat", p.lat);
            double alt = baseAlt + i * 5.0;
            wp.put("altitude", alt);
            wp.put("stopTime", 0);
            waypoints.add(wp);
        }
        return waypoints;
    }

    /** 生成路径飞行说明 */
    private List<String> generatePathInstructions(JSONArray path, double totalDistance) {
        List<String> instructions = new ArrayList<>();
        if (path == null || path.isEmpty()) return instructions;

        JSONObject startPoint = path.getJSONObject(0);
        instructions.add(String.format("从起点起飞，爬升至 %.0f 米高度", startPoint.getDoubleValue("altitude")));
        if (path.size() > 2) {
            instructions.add(String.format("沿优化路径飞行，途经 %d 个导航点", path.size() - 2));
        }
        instructions.add(String.format("保持巡航速度 %.1f m/s，总距离 %.1f km", UAV_SPEED, totalDistance / 1000));
        JSONObject endPoint = path.getJSONObject(path.size() - 1);
        instructions.add(String.format("接近目标区域，下降至 %.0f 米高度", endPoint.getDoubleValue("altitude")));
        instructions.add("到达目的地，完成飞行任务");
        return instructions;
    }

    /** 计算路径总距离 */
    public double calculatePathDistance(JSONArray path) {
        double total = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            JSONObject p1 = path.getJSONObject(i);
            JSONObject p2 = path.getJSONObject(i + 1);
            total += distance(p1.getDouble("lng"), p1.getDouble("lat"),
                    p2.getDouble("lng"), p2.getDouble("lat"));
        }
        return total;
    }

    /** 计算预计飞行时间 */
    public double calculateEstimatedTime(double distance) {
        return distance / UAV_SPEED;
    }

    /** 计算两点间距离（米） */
    public double distance(double lng1, double lat1, double lng2, double lat2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    // ---------- 内部类 ----------

    public static class RoutePlanningResult {
        private boolean success;
        private String message;
        private JSONArray path;
        private double totalDistance;
        private double estimatedTime;
        private int waypointCount;
        private List<String> instructions;

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

    private static class Point {
        double lng, lat;
        Point(double lng, double lat) { this.lng = lng; this.lat = lat; }
    }

    private static class RiskPoint {
        double lng, lat, weight;
        RiskPoint(double lng, double lat, double weight) { this.lng = lng; this.lat = lat; this.weight = weight; }
    }

    public static class Obstacle {
        public double lng, lat, radius;
        public Obstacle(double lng, double lat, double radius) {
            this.lng = lng; this.lat = lat; this.radius = radius;
        }
    }
}