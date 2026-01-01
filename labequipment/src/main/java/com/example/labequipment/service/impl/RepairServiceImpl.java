package com.example.labequipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.common.exception.CustomException;
import com.example.labequipment.dto.RepairRecordDTO;
import com.example.labequipment.dto.RepairRecordVO;
import com.example.labequipment.dto.ScrapRequestCreateDTO; // 引入报废DTO
import com.example.labequipment.entity.Device;
import com.example.labequipment.entity.RepairRecord;
import com.example.labequipment.entity.User;
import com.example.labequipment.mapper.DeviceMapper;
import com.example.labequipment.mapper.RepairRecordMapper;
import com.example.labequipment.mapper.UserMapper;
import com.example.labequipment.service.IRepairService;
import com.example.labequipment.service.IScrapRequestService; // 引入报废服务
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepairServiceImpl extends ServiceImpl<RepairRecordMapper, RepairRecord>
        implements IRepairService {

    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;
    private final IScrapRequestService scrapRequestService; // 注入报废服务
    private final RepairRecordMapper repairRecordMapper;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportRepair(RepairRecordDTO dto, Long userId) {
        // 1. 获取设备信息
        Device device = deviceMapper.selectById(dto.getDeviceId());
        User user = userMapper.selectById(userId);

        // 2. 权限校验：非 Admin 只能给本实验室设备报修
        if (!"admin".equals(user.getRole()) && !device.getLabId().equals(user.getLabId())) {
            throw new CustomException("无权操作其他实验室的设备");
        }

        // 3. 状态校验：不能给已报废或维修中的设备再次报修
        if ("scrapped".equals(device.getStatus()) || "under_repair".equals(device.getStatus())) {
            throw new CustomException("该设备当前状态不可报修");
        }

        // 4. 重复流程校验
        Long count = repairRecordMapper.selectCount(new LambdaQueryWrapper<RepairRecord>()
                .eq(RepairRecord::getDeviceId, dto.getDeviceId())
                .in(RepairRecord::getStatus, "reported", "assigned", "in_progress"));
        if (count > 0) {
            throw new CustomException("该设备已有活跃的维修工单");
        }
        RepairRecord record = new RepairRecord();
        BeanUtils.copyProperties(dto, record);
        record.setReporterId(userId);
        record.setStatus("reported");
        record.setReportedTime(LocalDateTime.now());

        device.setId(record.getDeviceId());
        device.setStatus("under_repair");
        deviceMapper.updateById(device);
        this.save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRepair(Long repairId, String status, Long managerId) {
        RepairRecord record = this.getById(repairId);
        if (record == null) throw new CustomException("工单不存在");

        if ("approved".equals(status)) {
            record.setStatus("assigned"); // 管理员知晓，安排维修
        } else {
            record.setStatus("rejected"); // 驳回
        }
        this.updateById(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startRepair(Long repairId) {
        RepairRecord record = this.getById(repairId);
        if (record == null) throw new CustomException("工单不存在");

        record.setStatus("in_progress");
        this.updateById(record);


    }


    /**
     * 维修结果录入（包含自动转报废逻辑）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeRepair(Long repairId, String solution, BigDecimal cost, String outcome) {
        // 1. 获取维修单信息
        RepairRecord record = this.getById(repairId);
        if (record == null) throw new CustomException("维修工单不存在");

        // 2. 更新工单状态为：已完成
        record.setSolution(solution);
        record.setCost(cost);
        record.setStatus("completed");
        record.setCompletedTime(LocalDateTime.now());
        this.updateById(record);

        // 3. 处理设备状态分支
        if ("fail".equals(outcome)) {
            // === 情况 A: 维修失败，自动发起报废申请 ===

            // 构造报废申请数据
            ScrapRequestCreateDTO scrapDto = new ScrapRequestCreateDTO();
            scrapDto.setDeviceId(record.getDeviceId());
            scrapDto.setReason("【维修失败自动转入】" + solution);

            // 调用报废服务的创建方法（userId 使用报修人的 ID）
            scrapRequestService.createScrapRequest(scrapDto, record.getReporterId());

            // 设备状态：建议此时保持 under_repair 状态，直到报废审批通过后由报废模块改为 scrapped
            // 如果你想让它显示得更特殊，可以加个 pending_scrap 状态（需数据库支持）
        } else {
            // === 情况 B: 维修成功，设备回库 ===
            Device device = new Device();
            device.setId(record.getDeviceId());
            device.setStatus("in_stock");
            deviceMapper.updateById(device);
        }
    }

    @Override
    public List<RepairRecordVO> getRepairList(String keyword) {
        Long userId = cn.dev33.satoken.stp.StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);

        LambdaQueryWrapper<RepairRecord> wrapper = new LambdaQueryWrapper<>();

        // 数据隔离逻辑
        if (!"admin".equals(user.getRole())) {
            List<Long> labDeviceIds = deviceMapper.selectList(
                    new LambdaQueryWrapper<Device>().eq(Device::getLabId, user.getLabId())
            ).stream().map(Device::getId).collect(Collectors.toList());

            if (labDeviceIds.isEmpty()) return new ArrayList<>();
            wrapper.in(RepairRecord::getDeviceId, labDeviceIds);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.like(RepairRecord::getDescription, keyword);
        }
        wrapper.orderByDesc(RepairRecord::getReportedTime);

        List<RepairRecord> list = this.list(wrapper);
        populateDetails(list);

        return list.stream().map(record -> {
            RepairRecordVO vo = new RepairRecordVO();
            BeanUtils.copyProperties(record, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    private void populateDetails(List<RepairRecord> list) {
        for (RepairRecord record : list) {
            Device d = deviceMapper.selectById(record.getDeviceId());
            if (d != null) {
                record.setDeviceName(d.getName());
                record.setDeviceAssetNumber(d.getAssetNumber());
            }
            User u = userMapper.selectById(record.getReporterId());
            if (u != null) {
                record.setReporterName(u.getUsername());
            }
        }
    }
}