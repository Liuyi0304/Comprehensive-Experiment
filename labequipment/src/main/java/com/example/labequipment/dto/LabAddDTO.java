package com.example.labequipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LabAddDTO {
    @NotBlank(message = "实验室名称不能为空")
    private String name;
    @NotBlank(message = "实验室位置不能为空")
    private String location;

    // 修改：前端下拉框选中后，传过来的是 managerId
    @NotNull(message = "请选择实验室负责人")
    private Long managerId;
}