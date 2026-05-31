package io.vehicle.vehicle_admin.service;

import io.vehicle.vehicle_admin.entity.Menu;
import io.vehicle.vehicle_admin.mapper.MenuMapper;
import io.vehicle.vehicle_admin.service.MenuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class MenuServiceImpl implements MenuService {
    @Resource
    private MenuMapper menuMapper;
    @Override
    public List<Menu> getMenuTree(){

        return menuMapper.getMenuTree();
    }
}

