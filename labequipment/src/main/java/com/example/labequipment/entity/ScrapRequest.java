package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("scrap_requests")
public class ScrapRequest {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;      // 关联设备ID
    private Long applicantId;   // 申请人ID
    private String reason;      // 报废原因

    // 状态: pending, approved, rejected
    private String status;

    private Long approvedBy;          // 审批人ID
    private LocalDateTime approvedAt; // 审批时间

    // ✅ 新增：对应数据库的 rejected_reason
    private String rejectedReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // --- 以下字段数据库不存在，仅用于前端展示 ---
    @TableField(exist = false)
    private String deviceName;    // 设备名称

    @TableField(exist = false)
    private String deviceAssetNumber; // 资产编号

    @TableField(exist = false)
    private String applicantName; // 申请人姓名
}