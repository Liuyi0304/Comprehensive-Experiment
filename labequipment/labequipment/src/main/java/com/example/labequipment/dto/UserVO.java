package com.example.labequipment.dto;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String realName;
    private String role; // ADMIN, STUDENT
}