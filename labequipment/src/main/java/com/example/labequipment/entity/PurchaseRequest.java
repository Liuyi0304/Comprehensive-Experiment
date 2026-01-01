// com/example/labequipment/entity/PurchaseRequest.java
package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("purchase_requests")
public class PurchaseRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long labId;
    private Long applicantId;
    private String deviceName;
    private Long categoryId;
    private String model;
    private String manufacturer;
    private Integer number = 1;
    private BigDecimal onePrice;
    private String reason;

    private String status; // "pending", "approved", "rejected"

    private Long approvedBy;
    private LocalDateTime approvedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}