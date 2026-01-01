// com/example/labequipment/dto/PurchaseRequestCreateDTO.java
package com.example.labequipment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PurchaseRequestCreateDTO {

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private String model;
    private String manufacturer;

    @Min(value = 1, message = "数量至少为1")
    private Integer number = 1;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.01", message = "单价必须大于0")
    private BigDecimal onePrice;

    @NotBlank(message = "申请理由不能为空")
    private String reason;
}