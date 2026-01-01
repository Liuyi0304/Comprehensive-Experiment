package com.example.labequipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.common.exception.CustomException;
import com.example.labequipment.dto.UsageRequestDTO;
import com.example.labequipment.entity.Device;
import com.example.labequipment.entity.UsageRecord;
import com.example.labequipment.entity.User;
import com.example.labequipment.mapper.DeviceMapper;
import com.example.labequipment.mapper.UsageRecordMapper;
import com.example.labequipment.mapper.UserMapper;
import com.example.labequipment.service.IUsageRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsageRecordServiceImpl extends ServiceImpl<UsageRecordMapper, UsageRecord>
        implements IUsageRecordService {

    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startUsage(UsageRequestDTO dto, Long userId) {
        // 1. 获取用户信息
        User user = userMapper.selectById(userId);

        // 2. 校验设备
        Device device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) throw new CustomException("设备不存在");

        // 3. 核心校验：必须是本实验室的设备
        if (!device.getLabId().equals(user.getLabId())) {
            throw new CustomException("只能登记本实验室的设备");
        }

        // 4. 状态校验：只有在库才能登记
        if (!"in_stock".equals(device.getStatus())) {
            throw new CustomException("设备当前状态不可领用（可能在修或在用）");
        }

        // 5. 创建记录
        UsageRecord record = new UsageRecord();
        record.setDeviceId(dto.getDeviceId());
        record.setUserId(userId);
        record.setLabId(user.getLabId());
        record.setStartTime(LocalDateTime.now());
        record.setPurpose(dto.getPurpose());
        record.setCreatedTime(LocalDateTime.now());
        this.save(record);

        // 6. 更新设备状态为“在用”
        device.setStatus("in_use");
        deviceMapper.updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void endUsage(Long recordId, Long userId) {
        UsageRecord record = this.getById(recordId);
        if (record == null) throw new CustomException("记录不存在");
        if (record.getEndTime() != null) throw new CustomException("该设备已归还");

        // 更新记录
        record.setEndTime(LocalDateTime.now());
        this.updateById(record);

        // 更新设备状态回“在库”
        Device device = deviceMapper.selectById(record.getDeviceId());
        if (device != null) {
            device.setStatus("in_stock");
            deviceMapper.updateById(device);
        }
    }

    @Override
    public List<UsageRecord> getUsageList(String keyword) {
        Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);

        LambdaQueryWrapper<UsageRecord> wrapper = new LambdaQueryWrapper<>();

        // 数据隔离：普通用户/主管只能看本实验室的领用记录
        if (!"admin".equals(user.getRole())) {
            wrapper.eq(UsageRecord::getLabId, user.getLabId());
        }

        wrapper.orderByDesc(UsageRecord::getCreatedTime);
        List<UsageRecord> list = this.list(wrapper);

        // 填充关联名称
        populateDetails(list);
        return list;
    }

    /**
     * 辅助方法：通过 ID 关联查询出设备名、资产编号和领用人姓名
     */
    private void populateDetails(List<UsageRecord> list) {
        if (list == null || list.isEmpty()) return;

        for (UsageRecord record : list) {
            // 1. 关联设备信息 (从 devices 表查)
            if (record.getDeviceId() != null) {
                Device d = deviceMapper.selectById(record.getDeviceId());
                if (d != null) {
                    // 对应 devices 表中的 name 字段
                    record.setDeviceName(d.getName());
                    // 对应 devices 表中的 asset_number 字段
                    record.setDeviceAssetNumber(d.getAssetNumber());
                }
            }

            // 2. 关联用户信息 (从 users 表查)
            if (record.getUserId() != null) {
                User u = userMapper.selectById(record.getUserId());
                if (u != null) {
                    // 优先取实名，如果没有实名就取用户名
                    String nameToDisplay = (u.getRealName() != null && !u.getRealName().isEmpty())
                            ? u.getRealName()
                            : u.getUsername();
                    record.setUserName(nameToDisplay);
                }
            }
        }
    }
}