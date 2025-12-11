package com.example.labequipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DeviceAddDTO {
    @NotBlank(message = "资产编号不能为空")
    private String assetNumber;

    @NotBlank(message = "设备名称不能为空")
    private String name;

    private String model;

    private String manufacturer;

    private BigDecimal price;

    private LocalDate purchaseDate; // 前端传字符串 "2023-12-01" 即可自动转换

    private Long categoryId; // 分类可以为空

    @NotNull(message = "必须选择所属实验室")
    private Long labId;

    // 默认状态，如果不传，后端 Service 层会处理
    private String status = "in_stock";
}