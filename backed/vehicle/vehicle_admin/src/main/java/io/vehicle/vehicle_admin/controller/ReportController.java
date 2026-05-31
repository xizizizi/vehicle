package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.entity.*;
import io.vehicle.vehicle_admin.mapper.*;
import io.vehicle.vehicle_admin.service.InspectionDataService;
import io.vehicle.vehicle_admin.service.ReportService;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Resource
    private ReportService reportService;

    @Resource
    private UavMapper uavMapper;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private CruiseMissionMapper missionMapper;

    @Resource
    private DispatchTaskMapper dispatchTaskMapper;

    @Resource
    private FlightTrackMapper flightTrackMapper;

    @Resource
    private AreaMapper areaMapper;
    // 在 ReportController 类中添加
    @Resource
    private InspectionDataService inspectionDataService;

    @Resource
    private InspectionDataMapper inspectionDataMapper;
    /**
     * 获取综合统计报告 - 修复版
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getReportStats() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 原有报告统计
            Map<String, Integer> reportStats = reportService.getReportStats();
            result.put("reportStats", reportStats);

            // 2. 无人机状态统计
            Map<String, Object> uavStats = getUavStats();
            result.put("uavStats", uavStats);

            // 3. 订单状态统计
            Map<String, Object> orderStats = getOrderStats();
            result.put("orderStats", orderStats);

            // 4. 任务状态统计
            Map<String, Object> missionStats = getMissionStats();
            result.put("missionStats", missionStats);

            // 5. 今日数据统计
            Map<String, Object> todayStats = getTodayStats();
            result.put("todayStats", todayStats);

            // 6. 区域统计 - 临时移除有问题的查询
            // List<Map<String, Object>> areaStats = areaMapper.selectAreaStats();
            // result.put("areaStats", areaStats);

            // 使用模拟区域数据
            List<Map<String, Object>> areaStats = new ArrayList<>();
            // 添加模拟数据...
            result.put("areaStats", areaStats);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            // 更友好的错误信息
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取统计报告失败");
            error.put("message", e.getMessage());
            error.put("timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(error);
        }
    }
    /**
     * 获取任务执行统计 - 简化版
     * 只返回前端需要的核心数据
     */
    @GetMapping("/task-execution-stats")
    public ResponseEntity<Map<String, Object>> getTaskExecutionStats() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 统计巡航任务
            List<CruiseMission> allMissions = missionMapper.selectList(null);
            long cruiseTotal = allMissions.size();
            long cruiseCompleted = allMissions.stream()
                    .filter(mission -> mission.getStatus() == CruiseMission.MissionStatus.COMPLETED)
                    .count();
            long cruiseActive = allMissions.stream()
                    .filter(mission -> mission.getStatus() == CruiseMission.MissionStatus.ACTIVE)
                    .count();
            long cruisePending = allMissions.stream()
                    .filter(mission -> mission.getStatus() == CruiseMission.MissionStatus.PENDING)
                    .count();

            // 2. 统计调度任务
            List<DispatchTask> allDispatchTasks = dispatchTaskMapper.selectList(null);
            long dispatchTotal = allDispatchTasks.size();
            long dispatchCompleted = allDispatchTasks.stream()
                    .filter(task -> task.getStatus() == DispatchTask.Status.COMPLETED)
                    .count();
            long dispatchActive = allDispatchTasks.stream()
                    .filter(task -> task.getStatus() == DispatchTask.Status.EXECUTING)
                    .count();
            long dispatchPending = allDispatchTasks.stream()
                    .filter(task -> task.getStatus() == DispatchTask.Status.PENDING)
                    .count();

            // 3. 统计巡检数据任务
            List<InspectionData> allInspectionData;
            try {
                Map<String, Object> params = new HashMap<>();
                allInspectionData = inspectionDataService.queryList(params);
            } catch (Exception e) {
                allInspectionData = inspectionDataMapper.selectList(null);
            }

            long inspectionTotal = allInspectionData.size();
            long inspectionCompleted = 0;
            long inspectionActive = 0;
            long inspectionPending = 0;

            for (InspectionData data : allInspectionData) {
                if (data.getStatus() == null) continue;

                String statusStr = data.getStatus().toString().toUpperCase();
                if (statusStr.contains("COMPLETED") || statusStr.contains("DONE") || "已完成".equals(data.getStatus().toString())) {
                    inspectionCompleted++;
                } else if (statusStr.contains("ACTIVE") || statusStr.contains("IN_PROGRESS") || statusStr.contains("EXECUTING") || "进行中".equals(data.getStatus().toString())) {
                    inspectionActive++;
                } else if (statusStr.contains("PENDING") || statusStr.contains("WAITING") || "待处理".equals(data.getStatus().toString())) {
                    inspectionPending++;
                }
            }

            // 4. 计算总数
            long totalTasks = cruiseTotal + dispatchTotal + inspectionTotal;
            long totalCompleted = cruiseCompleted + dispatchCompleted + inspectionCompleted;
            long totalActive = cruiseActive + dispatchActive + inspectionActive;
            long totalPending = cruisePending + dispatchPending + inspectionPending;

            // 5. 计算完成率
            double completionRate = totalTasks > 0 ?
                    (double) (totalTasks-totalPending-totalActive) / totalTasks * 100 : 0;

            // 6. 只返回前端需要的核心字段
            result.put("completionRate", Math.round(completionRate * 100.0) / 100.0);
            result.put("totalCompletedTasks", totalCompleted);
            result.put("pendingTasks", totalPending);
            result.put("activeTasks", totalActive);
            result.put("totalTasks", totalTasks);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取任务执行统计失败: " + e.getMessage());
            error.put("timestamp", LocalDateTime.now());
            return ResponseEntity.internalServerError().body(error);
        }
    }  /**
     * 创建任务类型详细统计
     */
    private Map<String, Object> createTaskTypeDetail(
            String name,
            long total,
            long completed,
            long active,
            long pending,
            long todayCompleted,
            long weekCompleted,
            long monthCompleted) {

        Map<String, Object> detail = new HashMap<>();
        detail.put("name", name);
        detail.put("total", total);
        detail.put("completed", completed);
        detail.put("active", active);
        detail.put("pending", pending);
        detail.put("todayCompleted", todayCompleted);
        detail.put("weekCompleted", weekCompleted);
        detail.put("monthCompleted", monthCompleted);

        // 计算完成率
        double completionRate = total > 0 ? (double) completed / total * 100 : 0;
        detail.put("completionRate", formatRate(completionRate));

        // 计算趋势（本月完成数 / 上周完成数）
        double growthRate = weekCompleted > 0 ?
                (double) todayCompleted / (weekCompleted / 7) * 100 - 100 : 0;
        detail.put("growthRate", formatRate(growthRate));

        return detail;
    }

    /**
     * 创建排名项目
     */
    private Map<String, Object> createRankingItem(
            String name,
            String type,
            long completed,
            long total) {

        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("type", type);
        item.put("completed", completed);
        item.put("total", total);

        double completionRate = total > 0 ? (double) completed / total * 100 : 0;
        item.put("completionRate", formatRate(completionRate));

        return item;
    }

    /**
     * 格式化百分比（保留两位小数）
     */
    private double formatRate(double rate) {
        return Math.round(rate * 100.0) / 100.0;
    }

    /**
     * 检查巡检任务是否已完成
     */
    private boolean isInspectionCompleted(Object status) {
        if (status == null) return false;
        String statusStr = status.toString().toUpperCase();
        return statusStr.contains("COMPLETED") ||
                statusStr.contains("DONE") ||
                statusStr.contains("FINISHED") ||
                "已完成".equals(status.toString());
    }

    /**
     * 检查巡检任务是否进行中
     */
    private boolean isInspectionActive(Object status) {
        if (status == null) return false;
        String statusStr = status.toString().toUpperCase();
        return statusStr.contains("ACTIVE") ||
                statusStr.contains("IN_PROGRESS") ||
                statusStr.contains("PROCESSING") ||
                statusStr.contains("EXECUTING") ||
                "进行中".equals(status.toString());
    }

    /**
     * 检查巡检任务是否待处理
     */
    private boolean isInspectionPending(Object status) {
        if (status == null) return false;
        String statusStr = status.toString().toUpperCase();
        return statusStr.contains("PENDING") ||
                statusStr.contains("WAITING") ||
                "待处理".equals(status.toString());
    }

    /**
     * 获取巡检任务的完成时间
     */
    private LocalDateTime getInspectionCompletedTime(InspectionData data) {
        // 优先使用更新时间，然后使用创建时间
        if (data.getUpdatedAt() != null) {
            return data.getUpdatedAt();
        }
        return data.getCreatedAt();
    }

    /**
     * 获取任务趋势数据
     */
    private Map<String, List<Object>> getTaskTrendData(LocalDateTime weekStart) {
        Map<String, List<Object>> trendData = new HashMap<>();

        // 获取最近7天的日期标签
        List<String> dateLabels = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            dateLabels.add(LocalDateTime.now().minusDays(i).toLocalDate().toString());
        }

        // 这里应该从数据库查询实际数据
        // 暂时返回模拟数据
        List<Object> dailyCompleted = Arrays.asList(8, 12, 9, 15, 11, 14, 10);
        List<Object> dailyActive = Arrays.asList(5, 4, 6, 5, 4, 3, 5);

        trendData.put("dates", Collections.singletonList(dateLabels));
        trendData.put("completed", dailyCompleted);
        trendData.put("active", dailyActive);

        return trendData;
    }
    /* 获取区域卡片数据 - 增强版
     */
    @GetMapping("/area-cards")
    public ResponseEntity<Map<String, Object>> getAreaCards() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 原有区域卡片数据
            List<Map<String, Object>> areaCards = reportService.getAreaCardData();
            result.put("areaCards", areaCards);

            // 2. 区域详细信息
            List<Area> allAreas = areaMapper.selectAllAreas();
            result.put("areaDetails", allAreas);

            // 3. 按类型分组的区域
            Map<String, Object> areasByType = getAreasByType();
            result.put("areasByType", areasByType);

            // 4. 区域任务分布
            Map<String, Object> areaMissionDistribution = getAreaMissionDistribution();
            result.put("areaMissionDistribution", areaMissionDistribution);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取区域卡片数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取最新任务 - 增强版
     */
    @GetMapping("/latest-tasks")
    public ResponseEntity<Map<String, Object>> getLatestTasks() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 原有最新任务
            List<Map<String, Object>> latestTasks = reportService.getLatestTasks();
            result.put("dispatchTasks", latestTasks);

            // 2. 最新无人机任务
            List<CruiseMission> latestMissions = missionMapper.selectActiveMissions();
            result.put("latestMissions", latestMissions);

            // 3. 最新订单
            List<Order> latestOrders = orderMapper.selectPendingOrders();
            result.put("latestOrders", latestOrders);

            // 4. 空闲无人机
            List<Uav> idleUavs = uavMapper.selectIdleUavsWithGoodBattery(30);
            result.put("idleUavs", idleUavs);

            // 5. 系统状态概览
            Map<String, Object> systemOverview = getSystemOverview();
            result.put("systemOverview", systemOverview);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取最新任务失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取玫瑰图任务数据 - 增强版
     */
    @GetMapping("/rose-tasks")
    public ResponseEntity<Map<String, Object>> getRoseTasks() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 原有玫瑰图任务数据
            List<Map<String, Object>> roseTasks = reportService.getRoseTasks();
            result.put("taskTypeDistribution", roseTasks);

            // 2. 任务状态分布
            Map<String, Object> missionStatusDistribution = getMissionStatusDistribution();
            result.put("missionStatusDistribution", missionStatusDistribution);

            // 3. 订单类型分布
            Map<String, Object> orderTypeDistribution = getOrderTypeDistribution();
            result.put("orderTypeDistribution", orderTypeDistribution);

            // 4. 无人机型号分布
            Map<String, Object> uavModelDistribution = getUavModelDistribution();
            result.put("uavModelDistribution", uavModelDistribution);

            // 5. 时间段任务分布
            Map<String, Object> timeSlotDistribution = getTimeSlotDistribution();
            result.put("timeSlotDistribution", timeSlotDistribution);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取玫瑰图数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * 获取排名数据 - 增强版
     */
    @GetMapping("/ranking")
    public ResponseEntity<Map<String, Object>> getRanking() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 原有排名数据
            List<Map<String, Object>> ranking = reportService.getRanking();
            result.put("reportRanking", ranking);

            // 2. 无人机任务完成排名
            List<Map<String, Object>> uavRanking = getUavMissionRanking();
            result.put("uavRanking", uavRanking);

            // 3. 区域活跃度排名
            List<Map<String, Object>> areaActivityRanking = getAreaActivityRanking();
            result.put("areaActivityRanking", areaActivityRanking);

            // 4. 任务类型完成排名
            List<Map<String, Object>> missionTypeRanking = getMissionTypeRanking();
            result.put("missionTypeRanking", missionTypeRanking);

            // 5. 最新轨迹数据
            List<Map<String, Object>> recentTracks = getRecentTracks();
            result.put("recentTracks", recentTracks);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "获取排名数据失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    // ========== 私有方法 ==========

    private Map<String, Object> getUavStats() {
        List<Uav> allUavs = uavMapper.selectList(null);
        Map<String, Long> tempMap = allUavs.stream()
                .collect(Collectors.groupingBy(
                        uav -> uav.getStatus() != null ? uav.getStatus().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        // 转换为 Map<String, Object>
        Map<String, Object> result = new HashMap<>();
        tempMap.forEach((key, value) -> result.put(key, value));
        return result;
    }

    private Map<String, Object> getOrderStats() {
        List<Order> allOrders = orderMapper.selectList(null);
        Map<String, Long> tempMap = allOrders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus() != null ? order.getStatus().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        tempMap.forEach((key, value) -> result.put(key, value));
        return result;
    }

    private Map<String, Object> getMissionStats() {
        List<CruiseMission> allMissions = missionMapper.selectList(null);
        Map<String, Long> tempMap = allMissions.stream()
                .collect(Collectors.groupingBy(
                        mission -> mission.getStatus() != null ? mission.getStatus().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        tempMap.forEach((key, value) -> result.put(key, value));
        return result;
    }

    private Map<String, Object> getTodayStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        // 今日新增订单
        List<Order> todayOrders = orderMapper.selectList(null).stream()
                .filter(order -> order.getCreatedTime() != null && order.getCreatedTime().isAfter(todayStart))
                .collect(Collectors.toList());
        stats.put("todayOrders", todayOrders.size());

        // 今日完成任务
        List<CruiseMission> todayCompletedMissions = missionMapper.selectList(null).stream()
                .filter(mission -> mission.getStatus() == CruiseMission.MissionStatus.COMPLETED &&
                        mission.getEndTime() != null && mission.getEndTime().isAfter(todayStart))
                .collect(Collectors.toList());
        stats.put("todayCompletedMissions", todayCompletedMissions.size());

        return stats;
    }

    private Map<String, Object> getAreasByType() {
        List<Area> allAreas = areaMapper.selectAllAreas();
        Map<String, List<Map<String, Object>>> tempMap = allAreas.stream()
                .collect(Collectors.groupingBy(
                        area -> area.getType() != null ? area.getType().name() : "UNKNOWN",
                        Collectors.mapping(this::convertAreaToMap, Collectors.toList())
                ));

        Map<String, Object> result = new HashMap<>();
        tempMap.forEach((key, value) -> result.put(key, value));
        return result;
    }

    private Map<String, Object> convertAreaToMap(Area area) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", area.getId());
        map.put("name", area.getName());
        map.put("type", area.getType());
        map.put("baseValue", area.getBaseValue());
        map.put("unit", area.getUnit());
        return map;
    }

    private Map<String, Object> getAreaMissionDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        // 这里可以实现区域任务分布逻辑
        distribution.put("commercial", 15);
        distribution.put("residential", 25);
        distribution.put("industrial", 20);
        distribution.put("natural", 30);
        distribution.put("special", 10);
        return distribution;
    }

    private Map<String, Object> getSystemOverview() {
        Map<String, Object> overview = new HashMap<>();

        List<Uav> uavs = uavMapper.selectList(null);
        List<Order> orders = orderMapper.selectList(null);
        List<CruiseMission> missions = missionMapper.selectList(null);

        overview.put("totalUavs", uavs.size());
        overview.put("totalOrders", orders.size());
        overview.put("totalMissions", missions.size());

        long activeMissions = missions.stream()
                .filter(m -> m.getStatus() == CruiseMission.MissionStatus.ACTIVE)
                .count();
        overview.put("activeMissions", activeMissions);

        return overview;
    }

    private Map<String, Object> getMissionStatusDistribution() {
        List<CruiseMission> missions = missionMapper.selectList(null);
        Map<String, Long> tempMap = missions.stream()
                .collect(Collectors.groupingBy(
                        mission -> mission.getStatus() != null ? mission.getStatus().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        tempMap.forEach((key, value) -> result.put(key, value));
        return result;
    }

    private Map<String, Object> getOrderTypeDistribution() {
        List<Order> orders = orderMapper.selectList(null);
        Map<String, Long> tempMap = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> {
                            if (order.getWeight() == null) return "UNKNOWN";
                            if (order.getWeight() < 1) return "LIGHT";
                            if (order.getWeight() < 3) return "MEDIUM";
                            return "HEAVY";
                        },
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        tempMap.forEach((key, value) -> result.put(key, value));
        return result;
    }

    private Map<String, Object> getUavModelDistribution() {
        List<Uav> uavs = uavMapper.selectList(null);
        Map<String, Long> tempMap = uavs.stream()
                .collect(Collectors.groupingBy(
                        uav -> uav.getModel() != null ? uav.getModel() : "UNKNOWN",
                        Collectors.counting()
                ));

        Map<String, Object> result = new HashMap<>();
        tempMap.forEach((key, value) -> result.put(key, value));
        return result;
    }

    private Map<String, Object> getTimeSlotDistribution() {
        Map<String, Object> distribution = new HashMap<>();
        distribution.put("morning", 35);
        distribution.put("afternoon", 45);
        distribution.put("evening", 20);
        return distribution;
    }

    private List<Map<String, Object>> getUavMissionRanking() {
        List<Uav> uavs = uavMapper.selectList(null);
        return uavs.stream()
                .map(uav -> {
                    Map<String, Object> rank = new HashMap<>();
                    rank.put("uavSn", uav.getSn());
                    rank.put("model", uav.getModel());
                    rank.put("completedMissions", new Random().nextInt(20)); // 模拟数据
                    rank.put("totalFlightTime", new Random().nextInt(1000)); // 模拟数据
                    return rank;
                })
                .sorted((a, b) -> Integer.compare(
                        (Integer) b.get("completedMissions"),
                        (Integer) a.get("completedMissions")
                ))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getAreaActivityRanking() {
        List<Area> areas = areaMapper.selectAllAreas();
        return areas.stream()
                .map(area -> {
                    Map<String, Object> rank = new HashMap<>();
                    rank.put("areaName", area.getName());
                    rank.put("areaType", area.getType());
                    rank.put("missionCount", new Random().nextInt(50)); // 模拟数据
                    rank.put("inspectionCount", new Random().nextInt(30)); // 模拟数据
                    return rank;
                })
                .sorted((a, b) -> Integer.compare(
                        (Integer) b.get("missionCount"),
                        (Integer) a.get("missionCount")
                ))
                .limit(10)
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getMissionTypeRanking() {
        List<CruiseMission> missions = missionMapper.selectList(null);
        Map<String, Long> tempMap = missions.stream()
                .collect(Collectors.groupingBy(
                        mission -> mission.getMissionType() != null ? mission.getMissionType().name() : "UNKNOWN",
                        Collectors.counting()
                ));

        return tempMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> rank = new HashMap<>();
                    rank.put("missionType", entry.getKey());
                    rank.put("count", entry.getValue());
                    return rank;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("count"),
                        (Long) a.get("count")
                ))
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getRecentTracks() {
        List<Uav> uavs = uavMapper.selectList(null);
        return uavs.stream()
                .limit(5) // 限制返回数量
                .map(uav -> {
                    List<FlightTrack> tracks = flightTrackMapper.selectRecentTracks(uav.getId(), 5);
                    Map<String, Object> trackInfo = new HashMap<>();
                    trackInfo.put("uavSn", uav.getSn());
                    trackInfo.put("recentTracks", tracks.stream()
                            .map(track -> {
                                Map<String, Object> point = new HashMap<>();
                                point.put("lng", track.getLng());
                                point.put("lat", track.getLat());
                                point.put("recordedTime", track.getRecordedTime());
                                return point;
                            })
                            .collect(Collectors.toList()));
                    return trackInfo;
                })
                .collect(Collectors.toList());
    }



}