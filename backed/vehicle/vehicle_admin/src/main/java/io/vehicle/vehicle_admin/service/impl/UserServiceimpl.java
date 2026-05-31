package io.vehicle.vehicle_admin.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.vehicle.vehicle_admin.entity.Role;
import io.vehicle.vehicle_admin.entity.User;
import io.vehicle.vehicle_admin.entity.UserRole;
import io.vehicle.vehicle_admin.mapper.RoleMapper;
import io.vehicle.vehicle_admin.mapper.UserMapper;
import io.vehicle.vehicle_admin.mapper.UserRoleMapper;
import io.vehicle.vehicle_admin.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static net.sf.jsqlparser.util.validation.metadata.NamedObject.user;

@Transactional
@Service
public class UserServiceimpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleMapper roleMapper;

    @Override
    public User login(User user){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("username",user.getUsername());
        queryWrapper.eq("password",user.getPassword());
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public User getUserInfoByToken(String token){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("token",token);
        User user = userMapper.selectOne(queryWrapper);

        QueryWrapper<UserRole> query = new QueryWrapper<>();
        queryWrapper.eq("user_id",user.getUserId());
        List<UserRole> userRoles = userRoleMapper.selectList(query);
        List<Integer> roleIds=userRoles.stream().map(UserRole::getRoleId).toList();
        List<Role> roles=roleMapper.selectBatchIds(roleIds);
        List<String>roleNames=roles.stream().map(Role::getRoleName).toList();
        user.setRoleNames(roleNames);
       // userRoleMapper.selectOne(query);

        return user;
    }

}

