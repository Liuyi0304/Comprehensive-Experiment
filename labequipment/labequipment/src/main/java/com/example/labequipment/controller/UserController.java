package com.example.labequipment.controller;

import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.UserAddDTO;
import com.example.labequipment.dto.UserUpdateDTO;
import com.example.labequipment.dto.UserVO;
import com.example.labequipment.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    /**
     * 新增用户
     */
    @PostMapping("/add")
    public Result<String> addUser(@RequestBody @Valid UserAddDTO dto) {
        userService.addUser(dto);
        return Result.success("用户添加成功");
    }

    /**
     * 获取用户列表
     * @param keyword 搜索关键字（可选，用户名或真实姓名）
     */
    @GetMapping("/list")
    public Result<List<UserVO>> list(@RequestParam(required = false) String keyword) {
        List<UserVO> list = userService.getUserList(keyword);
        return Result.success(list);
    }

    /**
     * 修改用户信息
     */
    @PutMapping("/update")
    public Result<String> updateUser(@RequestBody @Valid UserUpdateDTO dto) {
        userService.updateUser(dto);
        return Result.success("修改成功");
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("删除成功");
    }

    /**
     * 重置密码 (管理员强制重置用户密码)
     */
    @PostMapping("/reset-pwd/{id}")
    public Result<String> resetPassword(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success("密码已重置为 123456"); // 假设默认密码是这个
    }
}