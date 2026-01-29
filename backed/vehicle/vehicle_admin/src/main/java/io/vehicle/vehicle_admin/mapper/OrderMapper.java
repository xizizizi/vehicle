package io.vehicle.vehicle_admin.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM orders WHERE status = 'PENDING' AND deleted = 0 ORDER BY created_time DESC")
    List<Order> selectPendingOrders();
}
