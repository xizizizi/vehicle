package io.vehicle.vehicle_admin.mapper;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.vehicle.vehicle_admin.entity.User;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

/**
 * @author xixixizi
 * @version 2025/07 18
 * @since JDK22
 */

public interface UserMapper extends BaseMapper<User> {
    User selectOne(@Param("condition") Map<String, Object> condition);

    User findByToken(String token);
}
