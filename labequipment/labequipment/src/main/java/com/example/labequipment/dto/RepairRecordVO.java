package com.example.labequipment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RepairReportVO {
    private Long id;

    // 设备信息
    private Long deviceId;
    private String deviceName;
    private String deviceAssetNumber;

    // 报修人信息
    private Long reporterId;
    private String reporterName;

    // 维修信息
    private String description;
    private String status; // reported, in_progress, completed
    private String solution;
    private BigDecimal cost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportedTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedTime;
}