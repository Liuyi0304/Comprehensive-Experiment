package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("labs")
public class Lab {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String location;

    // 对应数据库 manager_id (外键关联 User)
    private Long managerId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
