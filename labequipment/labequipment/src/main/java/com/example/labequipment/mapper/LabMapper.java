package com.example.labequipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.labequipment.entity.Lab;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LabMapper extends BaseMapper<Lab> {
    // MyBatis Plus 会自动实现基础 CRUD，无需手写 SQL
}