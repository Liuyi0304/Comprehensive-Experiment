package com.example.labequipment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LabVO {
    private Long id;
    private String name;
    private String location;

    // 数据库存的是这个 ID
    private Long managerId;

    // 👇👇👇 重点：前端要显示的真实姓名
    private String managerRealName;

    private LocalDateTime createdTime;
}