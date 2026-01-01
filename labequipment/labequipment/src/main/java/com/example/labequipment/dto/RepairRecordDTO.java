package com.example.labequipment.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairReportDTO {
    @NotNull private Long deviceId;
    @NotNull private Long reporterId;
    @NotBlank private String description;
}
