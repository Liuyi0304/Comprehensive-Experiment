package com.example.labequipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.common.exception.CustomException;
import com.example.labequipment.dto.*;
import com.example.labequipment.entity.*;
import com.example.labequipment.mapper.*;
import com.example.labequipment.service.IDeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {

    private final UserMapper userMapper;
    private final DeviceMapper deviceMapper;
    // ✅ 补回：需要这两个Mapper来操作借用记录和采购单
    private final UsageRecordMapper usageRecordMapper;
    private final PurchaseRequestMapper purchaseRequestMapper;

    /**
     * 1. 获取设备列表
     */
    @Override
    public List<Device> getDeviceList(DeviceQueryDTO query, Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new CustomException("用户不存在");

        LambdaQueryWrapper<Device> wrapper = new LambdaQueryWrapper<>();

        // 权限隔离
        if ("admin".equals(user.getRole()) || "ROOT".equals(user.getRole())) {
            if (query.getLabId() != null) {
                wrapper.eq(Device::getLabId, query.getLabId());
            }
        } else {
            if (user.getLabId() == null) {
                throw new CustomException("您尚未绑定实验室，无法查看设备");
            }
            wrapper.eq(Device::getLabId, user.getLabId());
        }

        // 搜索条件
        if (StringUtils.isNotBlank(query.getName())) {
            wrapper.and(w -> w.like(Device::getName, query.getName())
                    .or().like(Device::getAssetNumber, query.getName()));
        }
        if (StringUtils.isNotBlank(query.getStatus())) {
            wrapper.eq(Device::getStatus, query.getStatus());
        }

        wrapper.orderByDesc(Device::getCreatedAt);
        return this.list(wrapper);
    }

    /**
     * 2. 借用设备 (✅ 补回来了)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void borrowDevice(Long deviceId, Long userId, String purpose) {
        Device device = this.getById(deviceId);
        if (device == null) throw new CustomException("设备不存在");

        // 只有 "in_stock" 状态才能借
        if (!"in_stock".equals(device.getStatus())) {
            throw new CustomException("设备当前不可借用 (状态: " + device.getStatus() + ")");
        }

        User user = userMapper.selectById(userId);

        // 1. 修改设备状态 -> in_use
        device.setStatus("in_use");
        this.updateById(device);

        // 2. 插入借用记录 (UsageRecord)
        UsageRecord record = new UsageRecord();
        record.setDeviceId(deviceId);
        record.setUserId(userId);
        record.setLabId(user.getLabId()); // 记录借用时的实验室
        record.setPurpose(purpose);

        // 时间字段 (对应数据库 start_time, created_time)
        record.setStartTime(LocalDateTime.now());
        record.setCreatedTime(LocalDateTime.now());

        usageRecordMapper.insert(record);
    }

    /**
     * 3. 归还设备 (✅ 补回来了)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnDevice(Long deviceId) {
        Device device = this.getById(deviceId);
        if (device == null) throw new CustomException("设备不存在");

        // 1. 修改设备状态 -> in_stock
        device.setStatus("in_stock");
        this.updateById(device);

        // 2. 找到最近一条未归还的记录 (end_time is null)
        LambdaQueryWrapper<UsageRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UsageRecord::getDeviceId, deviceId)
                .isNull(UsageRecord::getEndTime) // 未归还
                .orderByDesc(UsageRecord::getStartTime)
                .last("LIMIT 1");

        UsageRecord record = usageRecordMapper.selectOne(wrapper);
        if (record != null) {
            record.setEndTime(LocalDateTime.now()); // 更新归还时间
            usageRecordMapper.updateById(record);
        }
    }

    /**
     * 4. 提交采购申请 (✅ 之前新增的)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitPurchase(PurchaseRequestCreateDTO dto, Long userId) {
        PurchaseRequest request = new PurchaseRequest();
        BeanUtils.copyProperties(dto, request);

        request.setApplicantId(userId);
        request.setStatus("pending"); // 默认待审核
        request.setCreatedAt(LocalDateTime.now());

        purchaseRequestMapper.insert(request);
    }

    /**
     * 5. 管理员直接入库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDevice(DeviceAddDTO dto) {
        Long count = this.lambdaQuery().eq(Device::getAssetNumber, dto.getAssetNumber()).count();
        if (count > 0) throw new CustomException("资产编号 " + dto.getAssetNumber() + " 已存在");

        Device device = new Device();
        BeanUtils.copyProperties(dto, device);

        device.setStatus("in_stock");
        device.setCreatedAt(LocalDateTime.now());

        // 如果DTO没传购买日期，默认今天
        if (device.getPurchaseDate() == null) {
            device.setPurchaseDate(LocalDate.now());
        }

        this.save(device);
    }

    /**
     * 6. 设备调拨
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferDevice(DeviceTransferDTO dto) {
        Device device = this.getById(dto.getDeviceId());
        if (device == null) throw new CustomException("设备不存在");

        device.setLabId(dto.getToLabId());
        this.updateById(device);
    }

    /**
     * 7. 管理员直接报废
     */
    @Override
    public void adminDirectScrap(Long deviceId) {
        Device device = this.getById(deviceId);
        if (device == null) throw new CustomException("设备不存在");

        device.setStatus("scrapped");
        device.setScrappedAt(LocalDateTime.now()); // 记录报废时间
        this.updateById(device);
    }
}