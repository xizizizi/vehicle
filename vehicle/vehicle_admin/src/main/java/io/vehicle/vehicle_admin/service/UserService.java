package io.vehicle.vehicle_admin.service;


import io.vehicle.vehicle_admin.entity.User;
import org.apache.ibatis.annotations.Param;


public interface UserService {
    User login(User user);
    User getUserInfoByToken(String token);



}
