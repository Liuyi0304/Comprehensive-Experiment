package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("transfer_records") // 对应你的 SQL 表名（建议将 Untitled 改为 transfer_records）
public class TransferRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    private Long fromLabId;

    private Long toLabId;

    private Long operatorId; // 操作人ID

    private String reason;   // 调拨原因

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime transferTime; // 调拨时间
}