package com.example.labequipment.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceTransferDTO {
    @NotNull private Long deviceId;
    @NotNull private Long toLabId;
    @NotNull private Long operatorId;
    private String reason;
}
