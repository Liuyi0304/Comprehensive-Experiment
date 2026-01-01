package com.example.labequipment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserUpdateDTO {
    @NotNull(message = "用户ID不能为空")
    private Long id;

    private String realName;
    private String role;
    // 👇👇👇 必须加上这两个
    private String phone;
    private Long labId;
}