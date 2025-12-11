package com.example.labequipment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField; // 引入 TableField
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("users") // 你的 SQL 表名是 users，不是 sys_user
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    // ★★★ 关键修改 ★★★
    // 告诉 MyBatis Plus，Java 里的 'password' 对应数据库里的 'password_hash'
    @TableField("password_hash")
    private String password;

    // 你的 SQL 字段是 real_name，Java 是 realName，MyBatis Plus 默认开启驼峰转换，所以不需要动
    private String realName;

    private String role; // 对应 enum('admin','manager','user')

    // 你的 SQL 还有 phone, lab_id, created_time，如果需要用到也要加上
    private String phone;

    private Long labId;
}