package com.example.labequipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.common.exception.CustomException;
import com.example.labequipment.dto.*;
import com.example.labequipment.entity.User;
import com.example.labequipment.mapper.UserMapper;
import com.example.labequipment.service.IUserService;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
/**
 * 核心修正：必须 extends ServiceImpl<Mapper, 实体类>
 * 这样你就不用手动写 getById、save、update 等方法了。
 */
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    // 继承 ServiceImpl 后，你可以直接使用 baseMapper 或直接调用 list()、getById()

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        // 1. 使用内置的 LambdaQuery 获取用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));

        if (user == null) throw new CustomException("用户不存在");

        // 2. 校验密码 (数据库字段 password)
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new CustomException("密码错误");
        }

        // 3. Sa-Token 登录逻辑
        StpUtil.login(user.getId());

        Map<String, Object> map = new HashMap<>();
        map.put("token", StpUtil.getTokenValue());
        map.put("userId", user.getId());
        map.put("username", user.getUsername());
        map.put("role", user.getRole());
        map.put("labId", user.getLabId()); //
        return map;
    }

    @Override
    public void addUser(UserAddDTO dto) {
        // 使用内置的 count 方法检查
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));

        if (count > 0) throw new CustomException("用户名已存在");

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setCreatedTime(LocalDateTime.now());
        if (user.getRole() == null) user.setRole("student");

        this.save(user); // 使用 ServiceImpl 的 save 方法
    }

    @Override
    public List<UserVO> getUserList(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getRole, Arrays.asList("user", "manager", "student"));

        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword));
        }

        wrapper.orderByDesc(User::getCreatedTime);

        // 使用内置 list 方法
        return this.list(wrapper).stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateUser(UserUpdateDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        this.updateById(user); // 使用内置 updateById
    }

    @Override
    public void deleteUser(Long id) {
        this.removeById(id); // 使用内置 removeById
    }

    @Override
    public void resetPassword(Long id) {
        User user = new User();
        user.setId(id);
        user.setPassword("123456");
        this.updateById(user);
    }
}