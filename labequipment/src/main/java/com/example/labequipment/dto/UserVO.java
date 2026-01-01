package com.example.labequipment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String role; // ADMIN, STUDENT
    // 👇👇👇 必须补上这两个字段，名字要和 User Entity 一模一样 👇👇👇
    private String phone;
    private Long labId;

    // 建议把创建时间也加上，方便前端查看
    private LocalDateTime createdTime;
}