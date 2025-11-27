package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("repair_records")
public class RepairRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long deviceId;

    // 报修人ID
    private Long reporterId;

    // 维修处理人ID
    private Long handlerId;

    // 状态: reported(已上报), assigned(已指派), in_progress(维修中), completed(完成), rejected(驳回)
    private String status;

    private String description;

    private String solution;

    // 维修费用
    private BigDecimal cost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;
}
