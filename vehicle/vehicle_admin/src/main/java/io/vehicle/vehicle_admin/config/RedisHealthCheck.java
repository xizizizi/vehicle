package io.vehicle.vehicle_admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class RedisHealthCheck {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void checkRedisConnection() {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().get("test-connection");
                log.info("Redis连接正常");
            } catch (Exception e) {
                log.warn("Redis连接失败，将使用数据库模式运行: {}", e.getMessage());
            }
        } else {
            log.warn("RedisTemplate未配置，将使用数据库模式运行");
        }
    }
}