package com.example.labequipment.service;

import com.example.labequipment.dto.LoginDTO;
import com.example.labequipment.dto.UserAddDTO;
import com.example.labequipment.dto.UserUpdateDTO;
import com.example.labequipment.dto.UserVO;
import com.example.labequipment.entity.User;

import java.util.List;
import java.util.Map;

public interface IUserService {
    // 登录
    Map<String, Object> login(LoginDTO dto);

    // 新增用户
    void addUser(UserAddDTO dto);

    // 获取用户列表 (支持根据姓名或账号模糊搜索)
    List<UserVO> getUserList(String keyword);

    // 更新用户信息
    void updateUser(UserUpdateDTO dto);

    // 删除用户
    void deleteUser(Long id);

    // 重置密码 (通常重置为默认密码，如 123456)
    void resetPassword(Long id);

    User getById(Long id);
}