package com.example.labequipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.labequipment.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {

    /**
     * ✅ 手动添加这个方法，用于更新设备状态
     */
    @Update("UPDATE devices SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}