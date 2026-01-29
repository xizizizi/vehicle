package io.vehicle.vehicle_admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.vehicle.vehicle_admin.config.NanChangLocationConfig;
import io.vehicle.vehicle_admin.entity.Uav;
import io.vehicle.vehicle_admin.mapper.UavMapper;
import io.vehicle.vehicle_admin.service.UavService;
import io.vehicle.vehicle_admin.vo.UavAddVO;
import io.vehicle.vehicle_admin.vo.UavUpdateVO;
import io.vehicle.vehicle_admin.vo.UavQueryVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UavServiceImpl extends ServiceImpl<UavMapper, Uav> implements UavService {

    @Autowired
    private UavMapper uavMapper;

    @Autowired
    private NanChangLocationConfig nanChangLocationConfig;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Uav updateUav(Integer id, UavUpdateVO uavUpdateVO) {
        // 使用VO的验证方法
        String validationError = uavUpdateVO.validate();
        if (validationError != null) {
            throw new RuntimeException(validationError);
        }

        Uav uav = this.getById(id);
        if (uav == null) {
            throw new RuntimeException("无人机不存在，ID: " + id);
        }

        // 检查SN是否重复（如果不是当前无人机的SN）
        if (uavUpdateVO.getSn() != null && !uav.getSn().equals(uavUpdateVO.getSn())) {
            if (uavMapper.countBySn(uavUpdateVO.getSn()) > 0) {
                throw new RuntimeException("无人机序列号已存在: " + uavUpdateVO.getSn());
            }
        }

        // 更新属性
        if (uavUpdateVO.getSn() != null) {
            uav.setSn(uavUpdateVO.getSn());
        }
        if (uavUpdateVO.getModel() != null) {
            uav.setModel(uavUpdateVO.getModel());
        }
        if (uavUpdateVO.getStatus() != null) {
            uav.setStatus(uavUpdateVO.getStatus());
        }
        if (uavUpdateVO.getBatteryLevel() != null) {
            uav.setBatteryLevel(uavUpdateVO.getBatteryLevel());
        }
        if (uavUpdateVO.getCurrentMission() != null) {
            uav.setCurrentMission(uavUpdateVO.getCurrentMission());
        }
        if (uavUpdateVO.getLoadCapacity() != null) {
            uav.setLoadCapacity(uavUpdateVO.getLoadCapacity());
        }

        // 处理位置更新
        if (uavUpdateVO.getLocationName() != null && !uavUpdateVO.getLocationName().trim().isEmpty()) {
            NanChangLocationConfig.Location location = nanChangLocationConfig.getLocation(uavUpdateVO.getLocationName());
            if (location != null) {
                uav.setCurrentLng(location.getLng());
                uav.setCurrentLat(location.getLat());
            } else {
                throw new RuntimeException("找不到指定地点: " + uavUpdateVO.getLocationName());
            }
        } else if (uavUpdateVO.getCurrentLng() != null && uavUpdateVO.getCurrentLat() != null) {
            uav.setCurrentLng(uavUpdateVO.getCurrentLng());
            uav.setCurrentLat(uavUpdateVO.getCurrentLat());
        }

        if (this.updateById(uav)) {
            log.info("更新无人机成功: ID={}, SN={}", id, uav.getSn());
            return uav;
        } else {
            throw new RuntimeException("更新无人机失败");
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUav(Integer id) {
        Uav uav = this.getById(id);
        if (uav == null) {
            throw new RuntimeException("无人机不存在，ID: " + id);
        }

        // 物理删除 - 直接从数据库中删除记录
        return this.removeById(id);
    }

    @Override
    public Uav getUavById(Integer id) {
        return this.getById(id);
    }

    @Override
    public List<Uav> getAllUavs() {
        QueryWrapper<Uav> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        wrapper.orderByDesc("created_time");
        return this.list(wrapper);
    }

    @Override
    public List<Uav> getUavsByStatus(Uav.UavStatus status) {
        QueryWrapper<Uav> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        wrapper.eq("status", status.toString());
        wrapper.orderByDesc("created_time");
        return this.list(wrapper);
    }

    @Override
    public List<Uav> searchUavs(UavQueryVO queryVO) {
        QueryWrapper<Uav> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);

        if (StringUtils.hasText(queryVO.getSn())) {
            wrapper.like("sn", queryVO.getSn());
        }
        if (StringUtils.hasText(queryVO.getModel())) {
            wrapper.like("model", queryVO.getModel());
        }
        if (queryVO.getStatus() != null) {
            wrapper.eq("status", queryVO.getStatus().toString());
        }
        if (queryVO.getMinBattery() != null) {
            wrapper.ge("battery_level", queryVO.getMinBattery());
        }
        if (queryVO.getMaxBattery() != null) {
            wrapper.le("battery_level", queryVO.getMaxBattery());
        }

        wrapper.orderByDesc("created_time");
        return this.list(wrapper);
    }

    @Override
    public Map<String, Object> getUavStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 总数
        QueryWrapper<Uav> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        long total = this.count(wrapper);
        result.put("total", total);

        // 状态统计
        List<Map<String, Object>> statusStats = uavMapper.getStatusStatistics();
        result.put("statusStatistics", statusStats);

        // 电量统计
        wrapper.clear();
        wrapper.eq("deleted", 0);
        wrapper.lt("battery_level", 20);
        long lowBatteryCount = this.count(wrapper);
        result.put("lowBatteryCount", lowBatteryCount);

        // 最近7天新增
        wrapper.clear();
        wrapper.eq("deleted", 0);
        wrapper.ge("created_time", LocalDateTime.now().minusDays(7));
        long recent7DaysCount = this.count(wrapper);
        result.put("recent7DaysCount", recent7DaysCount);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Uav> batchImportUavs(List<UavAddVO> uavAddVOs) {
        List<Uav> successList = new ArrayList<>();
        List<String> errorMessages = new ArrayList<>();

        for (int i = 0; i < uavAddVOs.size(); i++) {
            try {
                UavAddVO vo = uavAddVOs.get(i);
                Uav uav = addUav(vo);
                successList.add(uav);
            } catch (Exception e) {
                errorMessages.add("第" + (i + 1) + "行: " + e.getMessage());
            }
        }

        if (!errorMessages.isEmpty()) {
            log.warn("批量导入无人机存在错误: {}", errorMessages);
            // 可以抛出自定义异常，包含错误信息
        }

        return successList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUavLocation(Integer id, Double lng, Double lat) {
        Uav uav = this.getById(id);
        if (uav == null) {
            throw new RuntimeException("无人机不存在，ID: " + id);
        }

        uav.setCurrentLng(lng);
        uav.setCurrentLat(lat);
        return this.updateById(uav);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUavStatus(Integer id, Uav.UavStatus status) {
        Uav uav = this.getById(id);
        if (uav == null) {
            throw new RuntimeException("无人机不存在，ID: " + id);
        }

        uav.setStatus(status);
        return this.updateById(uav);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUavBattery(Integer id, Integer batteryLevel) {
        Uav uav = this.getById(id);
        if (uav == null) {
            throw new RuntimeException("无人机不存在，ID: " + id);
        }

        if (batteryLevel < 0 || batteryLevel > 100) {
            throw new RuntimeException("电量必须在0-100之间");
        }

        uav.setBatteryLevel(batteryLevel);
        return this.updateById(uav);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Uav addUav(UavAddVO uavAddVO) {
        // 使用VO的验证方法
        String validationError = uavAddVO.validate();
        if (validationError != null) {
            throw new RuntimeException(validationError);
        }

        // 检查SN是否已存在
        if (uavMapper.countBySn(uavAddVO.getSn()) > 0) {
            throw new RuntimeException("无人机序列号已存在: " + uavAddVO.getSn());
        }

        // 创建无人机对象
        Uav uav = new Uav();
        BeanUtils.copyProperties(uavAddVO, uav);

        // 获取地点名称（支持多种字段名）
        String locationName = null;
        if (StringUtils.hasText(uavAddVO.getLocationName())) {
            locationName = uavAddVO.getLocationName();
        } else if (StringUtils.hasText(uavAddVO.getLocationName())) {
            locationName = uavAddVO.getLocationName();
        }

        // 处理位置信息
        if (locationName != null && !locationName.trim().isEmpty()) {
            log.info("尝试获取地点坐标: {}", locationName);

            // 根据地点名称获取经纬度 - 支持多种匹配方式
            NanChangLocationConfig.Location location = getLocationByName(locationName);

            if (location != null) {
                uav.setCurrentLng(location.getLng());
                uav.setCurrentLat(location.getLat());
                log.info("成功获取地点坐标: {} -> ({}, {})",
                        locationName, location.getLng(), location.getLat());
            } else {
                // 如果没找到，使用默认坐标
                log.warn("未找到地点: {}, 使用默认坐标", locationName);
                uav.setCurrentLng(115.857);  // 南昌默认经度
                uav.setCurrentLat(28.683);   // 南昌默认纬度
            }
        }

        // 设置默认值
        if (uav.getBatteryLevel() == null) {
            uav.setBatteryLevel(100);
        }
        if (uav.getStatus() == null) {
            uav.setStatus(Uav.UavStatus.IDLE);
        }
        if (uav.getLoadCapacity() == null) {
            uav.setLoadCapacity(5.0);
        }

        // 保存到数据库
        if (this.save(uav)) {
            log.info("添加无人机成功: SN={}, Model={}, Location={}",
                    uav.getSn(), uav.getModel(), locationName);
            return uav;
        } else {
            throw new RuntimeException("添加无人机失败");
        }
    }

    /**
     * 智能地点匹配方法
     */
    private NanChangLocationConfig.Location getLocationByName(String locationName) {
        if (locationName == null || nanChangLocationConfig == null) {
            return null;
        }

        // 1. 精确匹配
        NanChangLocationConfig.Location location = nanChangLocationConfig.getLocation(locationName);
        if (location != null) {
            return location;
        }

        // 2. 模糊匹配（去掉括号）
        String cleanName = locationName.replaceAll("\\(.*?\\)", "").trim();
        if (!cleanName.equals(locationName)) {
            location = nanChangLocationConfig.getLocation(cleanName);
            if (location != null) {
                log.info("通过清理括号找到地点: {} -> {}", locationName, cleanName);
                return location;
            }
        }

        // 3. 部分匹配（包含关系）
        Map<String, NanChangLocationConfig.Location> allLocations = nanChangLocationConfig.getLocations();
        for (Map.Entry<String, NanChangLocationConfig.Location> entry : allLocations.entrySet()) {
            String key = entry.getKey();
            // 如果配置的key包含输入的地点名，或输入的地点名包含配置的key
            if (key.contains(locationName) || locationName.contains(key)) {
                log.info("通过部分匹配找到地点: {} -> {}", locationName, key);
                return entry.getValue();
            }
        }

        // 4. 特殊处理：南昌大学第一附属医院(东湖院区)
        if (locationName.contains("南昌大学第一附属医院")) {
            // 使用南昌大学第一附属医院的坐标
            location = nanChangLocationConfig.getLocation("南昌大学第一附属医院");
            if (location != null) {
                log.info("使用南昌大学第一附属医院的坐标: {}", locationName);
                return location;
            }
        }

        return null;
    }
}