package com.example.labequipment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LabAddDTO {
    @NotBlank(message = "实验室名称不能为空")
    private String name;

    @NotBlank(message = "实验室位置不能为空")
    private String location;

    // 负责人ID是可选的，如果不选就是 null
    private Long managerId;
}