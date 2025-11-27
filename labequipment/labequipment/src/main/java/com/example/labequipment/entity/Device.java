package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("devices")
public class Device {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String assetNumber;
    private String name;
    private String model;
    private String manufacturer;
    private LocalDate purchaseDate;
    private BigDecimal price;
    private Long categoryId;
    private Long labId;

    // 状态: in_stock, in_use, under_repair, scrapped
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scrappedAt;
}
