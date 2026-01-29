package io.vehicle.vehicle_admin.service;

import io.vehicle.vehicle_admin.entity.Menu;

import java.util.List;

public interface MenuService {
    List<Menu> getMenuTree();
}