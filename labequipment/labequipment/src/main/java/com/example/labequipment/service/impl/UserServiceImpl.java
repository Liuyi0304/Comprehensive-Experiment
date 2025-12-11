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
import org.springframework.util.DigestUtils; // 简单的MD5加密演示，实际建议用BCrypt

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // 1. 这个注解配合 final 字段会自动生成构造函数，解决 userMapper 注入问题
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper; // 2. 声明 Mapper，解决 'Cannot resolve symbol userMapper'

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        // 1. 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = userMapper.selectOne(wrapper);

        // 2. 校验用户是否存在
        if (user == null) {
            // 使用自定义异常，抛出具体的业务错误信息
            throw new CustomException("用户不存在");
        }

        // 3. 校验密码
        if (!user.getPassword().equals(dto.getPassword())) {
            // 使用自定义异常
            throw new CustomException("密码错误");
        }

        // 4. 构造返回数据
        Map<String, Object> map = new HashMap<>();
        map.put("token", "dummy-token-" + user.getId());
        map.put("userId", user.getId());
        map.put("username", user.getUsername());
        map.put("role", user.getRole());
        return map;
    }
    /**
     * 实现新增用户方法
     * 解决 'must implement abstract method addUser' 报错
     */
    @Override
    public void addUser(UserAddDTO dto) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 转换对象
        User user = new User();
        BeanUtils.copyProperties(dto, user); // 解决 BeanUtils 报错

        // 3. 处理默认值和密码
        // 实际开发中密码一定要加密！这里演示存明文或者简单MD5
        // user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPassword(dto.getPassword());

        // 4. 保存
        userMapper.insert(user);
    }

    @Override
    public List<UserVO> getUserList(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword);
        }
        List<User> users = userMapper.selectList(wrapper);

        // 解决 Collectors 报错
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
        // MyBatis-Plus 会根据 ID 更新非空字段
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
        // 解决 'Cannot resolve method setPassword' -> 请确保 User 实体类加了 @Data
        userMapper.updateById(user);
    }
}