package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("transfer_records")
public class TransferRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long deviceId;
    private Long fromLabId;
    private Long toLabId;
    private Long operatorId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transferTime;

    private String reason;
}
