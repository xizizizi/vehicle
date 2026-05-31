package io.vehicle.vehicle_admin.controller;

import io.vehicle.vehicle_admin.dto.Result;
import io.vehicle.vehicle_admin.entity.Menu;
import io.vehicle.vehicle_admin.entity.User;
import io.vehicle.vehicle_admin.service.UserService;
import io.vehicle.vehicle_admin.service.MenuService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.lang.invoke.VarHandle;
import java.util.*;



/**
 *
 * @author xixixizi
 * @version 2025/7/18 19:01
 * @since JDK22
 */
@CrossOrigin
@RestController
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private MenuService menuService;


    @PostMapping(value = "/login")
    public Result login(@RequestBody User user){
            User login = userService.login(user);

            if(Objects.nonNull(login)){
                return Result.success(login);
            }else{
                return Result.fail("用户名或密码错误");
            }

        }

    @GetMapping("/getUserInfoByToken")
    public Result getUserInfoByToken(@RequestParam String token) {
        try {
            System.out.println("Received token: {}" + token); // 添加日志

            // 1. 验证token有效性
            if (token == null || token.isBlank()) {
                return Result.fail("Token不能为空");
            }

            // 2. 获取用户信息
            User user = userService.getUserInfoByToken(token);
            if (user == null) {
                System.out.println("未找到匹配用户, token: {}");
                return Result.fail("用户不存在或Token无效");
            }

            // 3. 获取菜单树 (添加空值保护)
            List<Menu> menuTree = menuService.getMenuTree();
            if (menuTree == null) {
                menuTree = Collections.emptyList(); // 防止空指针
            }

            // 4. 返回数据
            Map<String, Object> result = Map.of(
                    "user", user,
                    "menuTree", menuTree
            );
            return Result.success(result);

        } catch (Exception e) {
            System.out.println("未找到");
            return Result.fail("服务器内部错误: " + e.getMessage());
        }
    }}
