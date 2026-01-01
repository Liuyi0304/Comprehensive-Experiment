package com.example.labequipment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceTransferDTO {
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "目标实验室不能为空")
    private Long toLabId;

    @NotNull(message = "操作人不能为空")
    private Long operatorId;

    private String reason; // 选填
}