package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.entity.CruiseMission;
import io.vehicle.vehicle_admin.entity.Order;
import io.vehicle.vehicle_admin.entity.Uav;
import io.vehicle.vehicle_admin.mapper.CruiseMissionMapper;
import io.vehicle.vehicle_admin.mapper.OrderMapper;
import io.vehicle.vehicle_admin.mapper.UavMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UavMapper uavMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CruiseMissionMapper missionMapper;

    @GetMapping("/database")
    public Map<String, Object> checkDatabase() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 测试数据库连接和表数据
            List<Uav> allUavs = uavMapper.selectList(null);
            List<Order> allOrders = orderMapper.selectList(null);
            List<CruiseMission> allMissions = missionMapper.selectList(null);

            result.put("databaseStatus", "connected");
            result.put("totalUavs", allUavs.size());
            result.put("totalOrders", allOrders.size());
            result.put("totalMissions", allMissions.size());

            // 显示部分数据详情
            if (!allUavs.isEmpty()) {
                Uav sampleUav = allUavs.get(0);
                result.put("sampleUav", Map.of(
                        "id", sampleUav.getId(),
                        "sn", sampleUav.getSn(),
                        "status", sampleUav.getStatus(),
                        "deleted", sampleUav.getDeleted()
                ));
            }

        } catch (Exception e) {
            result.put("databaseStatus", "error");
            result.put("error", e.getMessage());
        }

        return result;
    }
}
