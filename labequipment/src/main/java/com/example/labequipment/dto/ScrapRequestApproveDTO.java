package com.example.labequipment.dto;
import lombok.Data;

@Data
public class ScrapRequestApproveDTO {
    private Long requestId;      // 申请单ID
    private String status;       // approved 或 rejected
    private String rejectedReason; // 驳回时必填的理由
}