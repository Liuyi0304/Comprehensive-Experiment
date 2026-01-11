package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("usage_records")
public class UsageRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private Long userId;
    private Long labId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private String purpose;
    private LocalDateTime createdTime;

    // ================= 🔴 新增：用于前端展示的非数据库字段 =================

    @TableField(exist = false) // 声明这不是数据库表里的字段
    private String deviceName;

    @TableField(exist = false)
    private String deviceAssetNumber; // 建议名字对应上，叫这个比较清晰

    @TableField(exist = false)
    private String userName;
}