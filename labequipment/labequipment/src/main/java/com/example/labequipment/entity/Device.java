package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("devices") // 对应数据库表名
public class Device {
    @TableId(type = IdType.AUTO)
    private Long id;

    // 资产编号 (全局唯一)
    private String assetNumber;

    // 设备名称
    private String name;

    // 设备型号
    private String model;

    // 生产商家
    private String manufacturer;

    // 购买日期 (对应 SQL date)
    private LocalDate purchaseDate;

    // 采购价格 (对应 SQL decimal(12,2))
    private BigDecimal price;

    // 设备分类ID
    private Long categoryId;

    // 所属实验室ID
    private Long labId;

    // 状态: in_stock, in_use, under_repair, scrapped
    // 对应 SQL 的 enum
    private String status;

    // 录入时间 (数据库自动维护，插入时填充)
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // 报废时间
    private LocalDateTime scrappedAt;
}