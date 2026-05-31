// ReportService.java
package io.vehicle.vehicle_admin.service;

import io.vehicle.vehicle_admin.entity.Area;
import io.vehicle.vehicle_admin.entity.ReportType;
import io.vehicle.vehicle_admin.mapper.ReportMapper;
import io.vehicle.vehicle_admin.mapper.ReportTypeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ReportTypeMapper reportTypeMapper;

    public Map<String, Integer> getReportStats() {
        List<ReportType> reportTypes = reportTypeMapper.findAll();
        Map<String, Integer> stats = new HashMap<>();
        for (ReportType reportType : reportTypes) {
            int count = reportMapper.countByReportType(reportType);
            stats.put(reportType.getTypeName(), count);
        }
        return stats;
    }

    public static class AreaStat {
        private Area.AreaType areaType;
        private BigDecimal totalArea;
        private Integer reportCount;

        public Area.AreaType getAreaType() { return areaType; }
        public void setAreaType(Area.AreaType areaType) { this.areaType = areaType; }
        public BigDecimal getTotalArea() { return totalArea; }
        public void setTotalArea(BigDecimal totalArea) { this.totalArea = totalArea; }
        public Integer getReportCount() { return reportCount; }
        public void setReportCount(Integer reportCount) { this.reportCount = reportCount; }
    }

    public List<Map<String, Object>> getAreaCardData() {
        List<AreaStat> stats = reportMapper.getAreaStats();
        int totalReports = stats.stream().mapToInt(AreaStat::getReportCount).sum();

        Map<Area.AreaType, AreaStat> statMap = stats.stream()
                .collect(Collectors.toMap(AreaStat::getAreaType, Function.identity()));

        return Arrays.stream(Area.AreaType.values())
                .filter(type -> Arrays.asList(
                        Area.AreaType.COMMERCIAL,
                        Area.AreaType.RESIDENTIAL,
                        Area.AreaType.INDUSTRIAL,
                        Area.AreaType.NATURAL,
                        Area.AreaType.SPECIAL
                ).contains(type))
                .map(type -> {
                    AreaStat stat = statMap.getOrDefault(type, new AreaStat());
                    return createCardData(type, stat, totalReports);
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> createCardData(Area.AreaType type, AreaStat stat, int totalReports) {
        Map<String, Object> card = new HashMap<>();
        card.put("title", type.getChineseName());

        BigDecimal area = stat.getTotalArea() != null ? stat.getTotalArea() : BigDecimal.ZERO;
        Map<String, Object> totalConfig = new HashMap<>();
        totalConfig.put("number", new int[]{area.intValue()});
        totalConfig.put("content", "{nt}");
        totalConfig.put("textAlign", "right");
        totalConfig.put("style", Map.of("fill", "#ea6027", "fontWeight", "bold"));
        card.put("total", totalConfig);

        int reportCount = stat.getReportCount() != null ? stat.getReportCount() : 0;
        Map<String, Object> numConfig = new HashMap<>();
        numConfig.put("number", new int[]{reportCount});
        numConfig.put("content", "{nt}");
        numConfig.put("textAlign", "right");
        numConfig.put("style", Map.of("fill", "#26fcd8", "fontWeight", "bold"));
        card.put("num", numConfig);

        int percentage = totalReports > 0 ?
                (int) Math.round(reportCount * 100.0 / totalReports) : 0;

        Map<String, Object> ring = new HashMap<>();
        ring.put("series", List.of(Map.of()));
        ring.put("color", List.of("#03d3ec"));
        card.put("ring", ring);

        return card;
    }

    public List<Map<String, Object>> getLatestTasks() {
        return reportMapper.getLatestTasks();
    }

    public List<Map<String, Object>> getRoseTasks() {
        return reportMapper.getRoseTasks();
    }

    public List<Map<String, Object>> getRanking() {
        return reportMapper.getRanking();
    }
}
