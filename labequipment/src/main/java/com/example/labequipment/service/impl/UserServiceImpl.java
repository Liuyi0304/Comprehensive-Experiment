package com.example.labequipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.labequipment.common.exception.CustomException;
import com.example.labequipment.dto.LoginDTO;
import com.example.labequipment.dto.UserAddDTO;
import com.example.labequipment.dto.UserUpdateDTO;
import com.example.labequipment.dto.UserVO;
import com.example.labequipment.entity.User;
import com.example.labequipment.mapper.UserMapper;
import com.example.labequipment.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // 记得加上这个，addUser里用到了
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;

    /**
     * ✅【新增修复】: 补上这个方法，解决 "abstract method getById" 报错
     */
    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        // 2. 校验用户是否存在
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        // 3. 校验密码
        if (!user.getPassword().equals(dto.getPassword())) {
            throw new CustomException("密码错误");
        }
        // 登录成功，记录 Session，这样 Controller 里才能取到 userId
        cn.dev33.satoken.stp.StpUtil.login(user.getId());
        // 3. 获取 Token 信息返回给前端
        String tokenValue = cn.dev33.satoken.stp.StpUtil.getTokenValue();
        // 4. 构造返回数据
        Map<String, Object> map = new HashMap<>();
        map.put("token", "dummy-token-" + user.getId()); // 简单模拟token
        map.put("userId", user.getId());
        map.put("username", user.getUsername());
        map.put("role", user.getRole());
        map.put("labId", user.getLabId()); // 建议加上这个，前端可能需要
        return map;
    }

    @Override
    public void addUser(UserAddDTO dto) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new CustomException("用户名已存在"); // 建议统一用 CustomException
        }

        // 2. 转换对象
        User user = new User();
        BeanUtils.copyProperties(dto, user);

        // 3. 处理默认值和密码
        user.setPassword(dto.getPassword());

        // 设置创建时间
        user.setCreatedTime(LocalDateTime.now());

        // 设置默认角色
        if (user.getRole() == null) {
            user.setRole("student");
        }

        // 4. 保存
        userMapper.insert(user);
    }

    @Override
    public List<UserVO> getUserList(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 1. 只查询普通用户和负责人，剔除 admin
        wrapper.in(User::getRole, Arrays.asList("user", "manager", "student")); // 加上 student 以防漏掉

        // 2. 搜索逻辑
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword));
        }

        // 按时间倒序
        wrapper.orderByDesc(User::getCreatedTime);

        // 3. 执行查询
        List<User> users = userMapper.selectList(wrapper);

        // 4. 转换结果
        return users.stream().map(u -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(u, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void updateUser(UserUpdateDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id) {
        User user = new User();
        user.setId(id);
        user.setPassword("123456"); // 重置为默认密码
        userMapper.updateById(user);
    }
}