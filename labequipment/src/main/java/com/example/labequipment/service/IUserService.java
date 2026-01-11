package com.example.labequipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.labequipment.dto.*;
import com.example.labequipment.entity.User;
import java.util.List;
import java.util.Map;

/**
 * 接口必须 extends IService<实体类>
 */
public interface IUserService extends IService<User> {
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

    // 重置密码
    void resetPassword(Long id);
}