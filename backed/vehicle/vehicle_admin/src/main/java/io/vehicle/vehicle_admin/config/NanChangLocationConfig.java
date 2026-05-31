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
        defaultLocations.put("江西省人民医院", new Location(115.879606,28.711791));
        defaultLocations.put("南昌大学第一附属医院", new Location(115.868863,28.597035));
        defaultLocations.put("南昌大学第二附属医院", new Location(115.842114,28.667088));
        defaultLocations.put("南昌市第一医院", new Location(115.896668,28.684614));
        defaultLocations.put("南昌市洪都中医院", new Location(115.899238,28.683892));
        defaultLocations.put("江西省儿童医院", new Location(115.902300,28.693298));
        defaultLocations.put("江西省妇幼保健院(九龙湖院区)", new Location(115.810491,28.627024));
        // 教育机构
        defaultLocations.put("南昌大学", new Location(115.807177,28.661912));
        defaultLocations.put("江西师范大学", new Location(116.038100,28.685261));
        defaultLocations.put("华东交通大学", new Location(115.876787,28.747183));
        defaultLocations.put("南昌航空大学", new Location(115.834196,28.656103));
        defaultLocations.put("南昌市第一中学", new Location(115.876291,28.629699));
        defaultLocations.put("南昌市第二中学", new Location(115.859801,28.705898));

        // 交通设施
        defaultLocations.put("南昌站", new Location(115.926112,28.668320));
        defaultLocations.put("南昌西站", new Location(115.799611,28.628861));
        defaultLocations.put("昌北机场", new Location(115.918639,28.864072));
        defaultLocations.put("南昌东站", new Location(116.027898,28.619678));
        defaultLocations.put("徐坊客运站", new Location(115.889, 28.663));

        // 景点
        defaultLocations.put("八一广场", new Location(115.910978,28.680175));
        defaultLocations.put("滕王阁", new Location(115.887051,28.686511));
        defaultLocations.put("秋水广场", new Location(115.869658,28.688643));
        defaultLocations.put("八一公园", new Location(115.903784,28.684104));
        defaultLocations.put("江西省博物馆", new Location(115.8884,28.7118));

        // 商业
        defaultLocations.put("红谷滩万达广场", new Location(115.855417,28.698629));
        defaultLocations.put("天虹商场", new Location(116.027845,28.680401));
        defaultLocations.put("中山路商业街", new Location(115.898978,28.681788));

        // 政府
        defaultLocations.put("南昌市公安局", new Location(115.867836,28.706614));

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