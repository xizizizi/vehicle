package io.vehicle.vehicle_admin.mapper;
/**
 * @author xixixizi
 * @version 2025/08 04
 * @since JDK22
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mysql.cj.log.Log;
import io.vehicle.vehicle_admin.entity.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
 class UserMapperTest {
    private static final Logger log = LoggerFactory.getLogger(UserMapperTest.class);
    @Resource
    private UserMapper userMapper;

    @Test
    void addUser(){
        User user = new User();
        user.setUsername("xixi");
        user.setPassword("111111");
        user.setToken(System.currentTimeMillis()+"");
        userMapper.insert(user);
    }
    @Test
    void getUser(){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username","xixi");
        queryWrapper.eq("password","111111");
        User user =userMapper.selectOne(queryWrapper);
        System.out.println(user);
 }

}

