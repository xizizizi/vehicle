package io.vehicle.vehicle_admin.service;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MqttMessageHandler implements MessageHandler {

    @Autowired
    private DispatchService dispatchService;

    @Override
    public void handleMessage(Message<?> message) throws MessagingException {
        try {
            String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC, String.class);
            String payload = message.getPayload().toString();

            log.info("收到MQTT消息 - Topic: {}, Payload: {}", topic, payload);

            // 处理无人机遥测数据
            if (topic != null && topic.startsWith("uav/telemetry/")) {
                String uavSn = topic.substring("uav/telemetry/".length());
                JSONObject telemetry = JSONObject.parseObject(payload);
                dispatchService.handleUavTelemetry(uavSn, telemetry);
            }

        } catch (Exception e) {
            log.error("处理MQTT消息失败", e);
        }
    }
}