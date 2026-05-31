package io.vehicle.vehicle_admin.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.vehicle.vehicle_admin.entity.Area;
import io.vehicle.vehicle_admin.mapper.AreaMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AreaService extends ServiceImpl<AreaMapper, Area> {

    public List<Area> getAllAreas() {
        return baseMapper.selectList(new QueryWrapper<>());
    }

    public Area getAreaById(Integer id) {
        return baseMapper.selectById(id);
    }

    public Area saveArea(Area area) {
        if (area.getId() == null) {
            baseMapper.insert(area);
        } else {
            baseMapper.updateById(area);
        }
        return area;
    }

    public void deleteArea(Integer id) {
        baseMapper.deleteById(id);
    }
}