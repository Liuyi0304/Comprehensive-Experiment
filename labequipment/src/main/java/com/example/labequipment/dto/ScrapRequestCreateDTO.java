package com.example.labequipment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ScrapRequestCreateDTO {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotBlank(message = "报废原因不能为空")
    private String reason;
}