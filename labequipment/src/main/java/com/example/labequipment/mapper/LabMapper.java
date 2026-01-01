package com.example.labequipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.labequipment.entity.Lab;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LabMapper extends BaseMapper<Lab> {
        // MyBatis Plus 会自动实现基础 CRUD，无需手写 SQL
        // 自定义查询：关联查询出负责人的真实姓名
        @Select("SELECT l.*, u.real_name AS managerRealName " +
                "FROM labs l " +
                "LEFT JOIN users u ON l.manager_id = u.id " +
                "ORDER BY l.created_time DESC")
        List<Lab> selectLabListWithManager();
}