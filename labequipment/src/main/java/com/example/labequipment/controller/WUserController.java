package com.example.labequipment.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.LoginDTO;
import com.example.labequipment.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import com.example.labequipment.entity.User;


@RestController
@RequestMapping("/weixinapi/user")
@RequiredArgsConstructor
public class WUserController {
    private final IUserService userService;

    @PostMapping("/login")
    public Result login(@RequestBody Map<String, String> loginInfo) {
        String username = loginInfo.get("username");
        String password = loginInfo.get("password");
        System.out.println("login ...");

        // 1. 根据用户名查询用户
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        // 2. 验证密码 (本地学习可以先用明文对比，实际项目用 password_hash)
        if (user != null && user.getPassword().equals(password)) {
            // 返回包含 role 的用户信息
            return Result.success(user);
        }
        return Result.error(Integer.valueOf("500"), "用户名或密码不正确");
    }

}
