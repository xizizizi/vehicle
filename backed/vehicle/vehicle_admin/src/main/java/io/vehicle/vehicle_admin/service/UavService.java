package io.vehicle.vehicle_admin.service;

import io.vehicle.vehicle_admin.entity.Uav;
import io.vehicle.vehicle_admin.vo.UavAddVO;
import io.vehicle.vehicle_admin.vo.UavUpdateVO;
import io.vehicle.vehicle_admin.vo.UavQueryVO;

import java.util.List;
import java.util.Map;

public interface UavService {

    /**
     * 添加无人机
     */
    Uav addUav(UavAddVO uavAddVO);

    /**
     * 更新无人机信息
     */
    Uav updateUav(Integer id, UavUpdateVO uavUpdateVO);

    /**
     * 删除无人机（逻辑删除）
     */
    boolean deleteUav(Integer id);

    /**
     * 根据ID获取无人机
     */
    Uav getUavById(Integer id);

    /**
     * 获取所有无人机
     */
    List<Uav> getAllUavs();

    /**
     * 根据状态查询无人机
     */
    List<Uav> getUavsByStatus(Uav.UavStatus status);

    /**
     * 搜索无人机
     */
    List<Uav> searchUavs(UavQueryVO queryVO);

    /**
     * 获取无人机统计信息
     */
    Map<String, Object> getUavStatistics();

    /**
     * 批量导入无人机
     */
    List<Uav> batchImportUavs(List<UavAddVO> uavAddVOs);

    /**
     * 更新无人机位置
     */
    boolean updateUavLocation(Integer id, Double lng, Double lat);

    /**
     * 更新无人机状态
     */
    boolean updateUavStatus(Integer id, Uav.UavStatus status);

    /**
     * 更新无人机电量
     */
    boolean updateUavBattery(Integer id, Integer batteryLevel);
}