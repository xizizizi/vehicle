package io.vehicle.vehicle_admin.mapper;

import io.vehicle.vehicle_admin.entity.Menu;
import io.vehicle.vehicle_admin.entity.Meta;
import io.vehicle.vehicle_admin.handler.MetaTypeHandler;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MenuMapperTest {
    @Resource
    private MenuMapper  menuMapper ;
    @Test
    void getMenuTree() {
        List<Menu> menuTree = menuMapper.getMenuTree();

        menuTree.forEach(System.out::println);
    }
}