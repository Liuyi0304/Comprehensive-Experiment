// com/example/labequipment/dto/PurchaseRequestApproveDTO.java
package com.example.labequipment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PurchaseRequestApproveDTO {

    @NotNull(message = "申请ID不能为空")
    private Long id;

    @NotBlank(message = "审批状态不能为空")
    @Pattern(regexp = "approved|rejected", message = "状态只能是 'approved' 或 'rejected'")

    private String status;
}