package io.vehicle.vehicle_admin.simulator;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class UavSimulator {

    @Value("${mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;

    private final Map<String, UavState> uavStates = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        log.info("开始初始化无人机模拟器...");

        try {
            // 初始化南昌地区的无人机
            simulateUav("NCUAV_001", 115.907, 28.662, "南昌站");
            simulateUav("NCUAV_002", 115.768, 28.620, "南昌西站");
            simulateUav("NCUAV_003", 115.900, 28.865, "昌北机场");
            simulateUav("NCUAV_004", 115.983, 28.625, "南昌东站");
            simulateUav("NCUAV_005", 115.889, 28.663, "徐坊客运站");

            log.info("无人机模拟器初始化完成，共 {} 架无人机", uavStates.size());
        } catch (Exception e) {
            log.error("无人机模拟器初始化失败", e);
        }
    }

    private void simulateUav(String uavSn, double baseLng, double baseLat, String location) {
        new Thread(() -> {
            MqttClient client = null;
            try {
                log.info("启动无人机模拟: {}", uavSn);

                String clientId = uavSn + "-simulator-" + System.currentTimeMillis();
                client = new MqttClient(brokerUrl, clientId);

                MqttConnectOptions options = new MqttConnectOptions();
                options.setCleanSession(true);
                options.setConnectionTimeout(10);
                options.setKeepAliveInterval(60);
                options.setAutomaticReconnect(true);

                client.connect(options);

                // 初始化无人机状态
                UavState state = new UavState();
                state.currentLng = baseLng;
                state.currentLat = baseLat;
                state.baseLng = baseLng;
                state.baseLat = baseLat;
                state.status = "IDLE";
                state.speed = 0;
                state.altitude = 50;
                state.battery = 80 + random.nextInt(20);
                uavStates.put(uavSn, state);

                // 订阅命令主题
                client.subscribe("uav/command/" + uavSn, (topic, message) -> {
                    String commandStr = new String(message.getPayload());
                    log.info("{} 收到命令: {}", uavSn, commandStr);
                    handleCommand(uavSn, commandStr, state);
                });

                log.info("无人机 {} 模拟器启动成功，开始发送遥测数据", uavSn);

                // 模拟发送遥测数据
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        updateUavPosition(uavSn, state);

                        JSONObject telemetry = new JSONObject();
                        telemetry.put("sn", uavSn);
                        telemetry.put("lng", state.currentLng);
                        telemetry.put("lat", state.currentLat);
                        telemetry.put("battery", state.battery);
                        telemetry.put("altitude", state.altitude);
                        telemetry.put("speed", state.speed);
                        telemetry.put("status", state.status);
                        telemetry.put("currentMission", state.currentMission);
                        telemetry.put("timestamp", System.currentTimeMillis());

                        MqttMessage mqttMessage = new MqttMessage(telemetry.toString().getBytes());
                        mqttMessage.setQos(1);
                        client.publish("uav/telemetry/" + uavSn, mqttMessage);

                        Thread.sleep(5000); // 每5秒发送一次，降低频率
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("无人机 {} 发送遥测数据异常: {}", uavSn, e.getMessage());
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

            } catch (Exception e) {
                log.error("无人机 {} 模拟异常: {}", uavSn, e.getMessage());
            } finally {
                if (client != null && client.isConnected()) {
                    try {
                        client.disconnect();
                        client.close();
                    } catch (Exception e) {
                        log.error("关闭MQTT客户端失败: {}", e.getMessage());
                    }
                }
            }
        }, "UavSimulator-" + uavSn).start();
    }

    private void handleCommand(String uavSn, String commandStr, UavState state) {
        try {
            JSONObject command = JSONObject.parseObject(commandStr);
            String cmdType = command.getString("command");

            log.info("无人机 {} 处理命令: {}", uavSn, cmdType);

            switch (cmdType) {
                case "START_CRUISE":
                    state.currentMission = "MISSION_" + command.getInteger("missionId");
                    state.status = "ON_MISSION";
                    state.cruiseRoute = command.getJSONArray("route");
                    state.currentRouteIndex = 0;
                    state.cruiseSpeed = command.getDoubleValue("cruiseSpeed");
                    state.altitude = command.getDoubleValue("altitude");
                    log.info("无人机 {} 开始巡航任务", uavSn);
                    break;

                case "START_DELIVERY":
                    state.currentMission = "ORDER_" + command.getInteger("orderId");
                    state.status = "ON_MISSION";
                    JSONObject pickup = command.getJSONObject("pickup");
                    JSONObject delivery = command.getJSONObject("delivery");
                    state.targetLng = pickup.getDouble("lng");
                    state.targetLat = pickup.getDouble("lat");
                    state.deliveryTargetLng = delivery.getDouble("lng");
                    state.deliveryTargetLat = delivery.getDouble("lat");
                    state.speed = command.getDoubleValue("speed");
                    state.altitude = command.getDoubleValue("altitude");
                    log.info("无人机 {} 开始配送任务", uavSn);
                    break;

                case "RETURN_BASE":
                    state.currentMission = null;
                    state.status = "RETURNING";
                    state.targetLng = state.baseLng;
                    state.targetLat = state.baseLat;
                    log.info("无人机 {} 返回基地", uavSn);
                    break;

                case "MISSION_COMPLETE":
                    state.currentMission = null;
                    state.status = "IDLE";
                    state.speed = 0;
                    log.info("无人机 {} 任务完成", uavSn);
                    break;
            }

        } catch (Exception e) {
            log.error("处理命令失败: {}", e.getMessage());
        }
    }

    private void updateUavPosition(String uavSn, UavState state) {
        // 模拟电量消耗
        if (state.battery > 0 && random.nextInt(10) == 0) {
            state.battery--;
        }

        if ("ON_MISSION".equals(state.status) && state.cruiseRoute != null) {
            updateCruisePosition(state);
        } else if ("ON_MISSION".equals(state.status) && state.targetLng != null) {
            updateDeliveryPosition(state);
        } else if ("RETURNING".equals(state.status)) {
            updateReturnPosition(state);
        } else {
            // 空闲模式，在基地附近小范围移动
            state.currentLng = state.baseLng + (random.nextDouble() - 0.5) * 0.001;
            state.currentLat = state.baseLat + (random.nextDouble() - 0.5) * 0.001;
            state.speed = 0.5 + random.nextDouble() * 1.5;
            state.altitude = 50 + random.nextInt(30);
        }
    }

    private void updateCruisePosition(UavState state) {
        if (state.cruiseRoute == null || state.cruiseRoute.isEmpty()) return;

        JSONArray route = state.cruiseRoute;
        int currentIndex = state.currentRouteIndex;
        JSONObject currentPoint = route.getJSONObject(currentIndex);

        double targetLng = currentPoint.getDouble("lng");
        double targetLat = currentPoint.getDouble("lat");

        // 简单的位置逼近算法
        moveTowardsTarget(state, targetLng, targetLat, state.cruiseSpeed);

        // 如果接近目标点，切换到下一个点
        double distance = calculateDistance(state.currentLng, state.currentLat, targetLng, targetLat);
        if (distance < 50) { // 50米范围内
            state.currentRouteIndex = (currentIndex + 1) % route.size();
        }

        state.speed = state.cruiseSpeed;
    }

    private void updateDeliveryPosition(UavState state) {
        if (state.targetLng == null) return;

        double targetLng = state.targetLng;
        double targetLat = state.targetLat;

        moveTowardsTarget(state, targetLng, targetLat, state.speed);

        double distance = calculateDistance(state.currentLng, state.currentLat, targetLng, targetLat);
        if (distance < 30) { // 30米范围内
            // 到达取件点或配送点
            if (state.deliveryTargetLng != null &&
                    Math.abs(targetLng - state.deliveryTargetLng) < 0.0001) {
                // 到达配送点，任务完成
                state.currentMission = null;
                state.status = "IDLE";
                state.speed = 0;
                state.targetLng = null;
                state.targetLat = null;
            } else {
                // 到达取件点，前往配送点
                state.targetLng = state.deliveryTargetLng;
                state.targetLat = state.deliveryTargetLat;
                log.info("无人机已取件，前往配送点");
            }
        }
    }

    private void updateReturnPosition(UavState state) {
        moveTowardsTarget(state, state.baseLng, state.baseLat, 8.0);

        double distance = calculateDistance(state.currentLng, state.currentLat, state.baseLng, state.baseLat);
        if (distance < 20) {
            state.status = "IDLE";
            state.currentMission = null;
            state.speed = 0;
            state.targetLng = null;
            state.targetLat = null;
        }
    }

    private void moveTowardsTarget(UavState state, double targetLng, double targetLat, double speed) {
        double currentLng = state.currentLng;
        double currentLat = state.currentLat;

        double dlng = targetLng - currentLng;
        double dlat = targetLat - currentLat;
        double distance = Math.sqrt(dlng * dlng + dlat * dlat);

        if (distance > 0) {
            double step = Math.min(speed * 0.00001, distance);
            state.currentLng = currentLng + dlng * step / distance;
            state.currentLat = currentLat + dlat * step / distance;
        }
    }

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

    // 无人机状态内部类
    private static class UavState {
        double currentLng;
        double currentLat;
        double baseLng;
        double baseLat;
        Double targetLng;
        Double targetLat;
        Double deliveryTargetLng;
        Double deliveryTargetLat;
        String status = "IDLE";
        String currentMission;
        JSONArray cruiseRoute;
        Integer currentRouteIndex = 0;
        double speed = 0;
        double altitude = 50;
        double cruiseSpeed = 8.0;
        int battery = 100;
    }
}