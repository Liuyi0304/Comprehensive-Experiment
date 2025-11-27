package com.example.labequipment.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.labequipment.dto.LoginDTO;
import com.example.labequipment.entity.User;
import java.util.Map;

public interface IUserService extends IService<User> {
    Map<String, Object> login(LoginDTO dto);
}
