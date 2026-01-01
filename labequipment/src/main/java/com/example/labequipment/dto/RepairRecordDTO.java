package com.example.labequipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RepairRecordDTO {

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotBlank(message = "故障描述不能为空")
    private String description;

    // ✅ 解决方法：删掉 @NotNull 注解，或者直接删掉这个字段
    // 因为这个 ID 我们在 Controller 里通过 StpUtil 获取，不需要前端传
    private Long reporterId;
}