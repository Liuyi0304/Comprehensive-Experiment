package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("repair_records")
public class RepairRecord {
    private Long id;
    private Long deviceId;
    private Long reporterId;
    private String description;
    private String status;
    private LocalDateTime reportedTime;
    private String solution;
    private BigDecimal cost;
    private LocalDateTime completedTime;

    // 以下为非数据库字段，用于前端展示
    @TableField(exist = false)
    private String deviceName;
    @TableField(exist = false)
    private String deviceAssetNumber;
    @TableField(exist = false)
    private String reporterName;
}
