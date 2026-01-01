package com.example.labequipment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserAddDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    // 角色，例如 "ADMIN" 或 "STUDENT"，默认 STUDENT
    private String role = "user";
    // 👇👇👇 必须加上这两个
    private String phone;
    private Long labId;
}