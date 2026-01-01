package com.example.labequipment.dto;

import lombok.Data;

@Data
public class LabUpdateDTO {
    private Long id;
    private String name;
    private String location;

    // 👇👇👇 必须有这个，否则编辑提交时，后端收不到 ID，就会把负责人置空
    private Long managerId;
}