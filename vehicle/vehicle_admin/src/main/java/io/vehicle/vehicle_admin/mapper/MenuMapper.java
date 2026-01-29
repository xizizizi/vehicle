package io.vehicle.vehicle_admin.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.Menu;

import java.util.List;

public interface MenuMapper extends BaseMapper<Menu> {

    List<Menu> getMenuTree();

    List<Menu> getSubMenuList(Integer menuId);
}
