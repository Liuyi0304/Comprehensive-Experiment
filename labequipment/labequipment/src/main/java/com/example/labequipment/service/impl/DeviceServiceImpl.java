package com.example.labequipment.service.impl;
import com.baomidou.mybatisplus.core.assist.ISqlRunner;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.common.exception.CustomException;
import com.example.labequipment.dto.DeviceAddDTO;
import com.example.labequipment.dto.DeviceTransferDTO;
import com.example.labequipment.entity.*;
import com.example.labequipment.mapper.*;
import com.example.labequipment.service.IDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {
    private final UsageRecordMapper usageRecordMapper;
    private final TransferRecordMapper transferRecordMapper;
    private final DeviceMapper deviceMapper;
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
    @Transactional(rollbackFor = Exception.class) // 开启事务，保证两步操作同时成功
    public void transferDevice(DeviceTransferDTO dto) {
        // 1. 查询设备当前信息
        Device device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) {
            throw new CustomException("设备不存在");
        }

        // 记录旧的实验室ID
        Long oldLabId = device.getLabId();
        if (oldLabId.equals(dto.getToLabId())) {
            throw new CustomException("目标实验室与当前实验室相同，无需调拨");
        }

        // 2. 更新设备表 (devices) -> 修改 lab_id
        device.setLabId(dto.getToLabId());
        deviceMapper.updateById(device);

        // 3. 插入调拨记录表 (transfer_records)
        TransferRecord record = new TransferRecord();
        record.setDeviceId(device.getId());
        record.setFromLabId(oldLabId);      // 从刚才查出来的旧ID拿
        record.setToLabId(dto.getToLabId()); // 从前端传的新ID拿
        record.setOperatorId(dto.getOperatorId());
        record.setReason(dto.getReason());
        record.setTransferTime(LocalDateTime.now());

        transferRecordMapper.insert(record);
    }

    @Override
    public List<Device> list() {
        return deviceMapper.selectList(null);
    }

    @Override
    public void addDevice(DeviceAddDTO dto) {
        // 1. 校验资产编号唯一性 (对应 SQL UNIQUE INDEX `asset_number`)
        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Device::getAssetNumber, dto.getAssetNumber());
        if (deviceMapper.selectCount(wrapper) > 0) {
            throw new CustomException("资产编号 " + dto.getAssetNumber() + " 已存在");
        }

        // 2. 对象转换
        Device device = new Device();
        BeanUtils.copyProperties(dto, device);

        // 3. 设置默认值
        // SQL 定义了 DEFAULT 'in_stock'，但为了保险，代码里也显式设置
        if (device.getStatus() == null) {
            device.setStatus("in_stock");
        }

        // createdAt 由 MyBatisPlus 自动填充或数据库默认值处理
        device.setCreatedAt(LocalDateTime.now());

        // 4. 插入数据库
        deviceMapper.insert(device);
    }
}
