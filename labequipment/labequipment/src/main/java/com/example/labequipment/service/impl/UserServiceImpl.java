package com.example.labequipment.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.common.utils.JwtUtils;
import com.example.labequipment.dto.LoginDTO;
import com.example.labequipment.entity.User;
import com.example.labequipment.mapper.UserMapper;
import com.example.labequipment.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    private final JwtUtils jwtUtils;

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) throw new RuntimeException("用户不存在");
        // 这里简化为明文对比，实际建议用 BCrypt
        if (!dto.getPassword().equals(user.getPasswordHash())) throw new RuntimeException("密码错误");

        String token = jwtUtils.generateToken(user.getId(), user.getRole());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("user", user);
        return map;
    }
}
