package com.example.labequipment.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.dto.DeviceTransferDTO;
import com.example.labequipment.entity.*;
import com.example.labequipment.mapper.*;
import com.example.labequipment.service.IDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {
    private final UsageRecordMapper usageRecordMapper;
    private final TransferRecordMapper transferRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void borrowDevice(Long deviceId, Long userId, String purpose) {
        Device device = this.getById(deviceId);
        if (device == null || !"in_stock".equals(device.getStatus())) {
            throw new RuntimeException("设备不存在或状态不可借用");
        }
        device.setStatus("in_use");
        this.updateById(device);

        UsageRecord record = new UsageRecord();
        record.setDeviceId(deviceId);
        record.setUserId(userId);
        record.setLabId(device.getLabId());
        record.setStartTime(LocalDateTime.now());
        record.setPurpose(purpose);
        record.setCreatedTime(LocalDateTime.now());
        usageRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnDevice(Long deviceId) {
        Device device = this.getById(deviceId);
        if (device == null || !"in_use".equals(device.getStatus())) throw new RuntimeException("设备未被借出");

        device.setStatus("in_stock");
        this.updateById(device);
        // 实际逻辑应更新对应UsageRecord的endTime，此处简化
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferDevice(DeviceTransferDTO dto) {
        Device device = this.getById(dto.getDeviceId());
        if (device == null) throw new RuntimeException("设备不存在");

        TransferRecord record = new TransferRecord();
        record.setDeviceId(dto.getDeviceId());
        record.setFromLabId(device.getLabId());
        record.setToLabId(dto.getToLabId());
        record.setOperatorId(dto.getOperatorId());
        record.setTransferTime(LocalDateTime.now());
        record.setReason(dto.getReason());
        transferRecordMapper.insert(record);

        device.setLabId(dto.getToLabId());
        this.updateById(device);
    }
}
