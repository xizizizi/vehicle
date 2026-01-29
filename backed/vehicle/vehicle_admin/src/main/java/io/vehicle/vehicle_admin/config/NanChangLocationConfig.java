package io.vehicle.vehicle_admin.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "nanchang.locations")
public class NanChangLocationConfig {

    private Map<String, Location> locations = new HashMap<>();

    @PostConstruct
    public void init() {
        log.info("初始化南昌地点配置...");

        // 如果配置文件没有加载到地点，使用默认值
        if (locations == null || locations.isEmpty()) {
            log.warn("配置文件中的地点为空，使用默认地点配置");
            locations = createDefaultLocations();
        }

        log.info("已加载地点数量: {}", locations.size());
        for (Map.Entry<String, Location> entry : locations.entrySet()) {
            log.info("地点: {} -> 经度: {}, 纬度: {}",
                    entry.getKey(), entry.getValue().getLng(), entry.getValue().getLat());
        }
    }

    public Location getLocation(String name) {
        return locations.get(name);
    }

    public Map<String, Location> getLocations() {
        return locations;
    }

    /**
     * 创建默认地点配置（使用真实的南昌地点）
     */
    private Map<String, Location> createDefaultLocations() {
        Map<String, Location> defaultLocations = new HashMap<>();

        // 医疗设施
        defaultLocations.put("江西省人民医院", new Location(115.8996, 28.6791));
        defaultLocations.put("南昌大学第一附属医院", new Location(115.8572, 28.6845));
        defaultLocations.put("南昌大学第二附属医院", new Location(115.904367, 28.674892));
        defaultLocations.put("南昌市第一医院", new Location(115.892578, 28.681945));
        defaultLocations.put("南昌市洪都中医院", new Location(115.8893, 28.6634));
        defaultLocations.put("江西省儿童医院", new Location(115.9043, 28.6718));

        // 教育机构
        defaultLocations.put("南昌大学", new Location(115.8129, 28.6602));
        defaultLocations.put("江西师范大学", new Location(115.9381, 28.6754));
        defaultLocations.put("华东交通大学", new Location(115.857234, 28.749856));
        defaultLocations.put("南昌航空大学", new Location(115.826745, 28.648912));
        defaultLocations.put("南昌市第一中学", new Location(115.8934, 28.6782));
        defaultLocations.put("南昌市第二中学", new Location(115.8387, 28.6842));

        // 交通设施
        defaultLocations.put("南昌站", new Location(115.9187, 28.6629));
        defaultLocations.put("南昌西站", new Location(115.7844, 28.6208));
        defaultLocations.put("昌北机场", new Location(115.9009, 28.8648));
        defaultLocations.put("南昌东站", new Location(115.983, 28.625));
        defaultLocations.put("徐坊客运站", new Location(115.889, 28.663));

        // 景点
        defaultLocations.put("八一广场", new Location(115.9083, 28.6766));
        defaultLocations.put("滕王阁", new Location(115.8806, 28.6857));
        defaultLocations.put("秋水广场", new Location(115.8572, 28.6892));
        defaultLocations.put("八一公园", new Location(115.8976, 28.6812));

        // 商业
        defaultLocations.put("红谷滩万达广场", new Location(115.8512, 28.6893));
        defaultLocations.put("天虹商场", new Location(115.8578, 28.6821));
        defaultLocations.put("中山路商业街", new Location(115.8932, 28.6794));

        // 政府
        defaultLocations.put("南昌市公安局", new Location(115.8921, 28.6834));

        return defaultLocations;
    }

    @Data
    public static class Location {
        private Double lng;
        private Double lat;

        public Location() {}

        public Location(Double lng, Double lat) {
            this.lng = lng;
            this.lat = lat;
        }
    }
}