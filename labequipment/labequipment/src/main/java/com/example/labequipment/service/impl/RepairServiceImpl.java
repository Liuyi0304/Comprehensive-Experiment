package com.example.labequipment.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.dto.RepairReportDTO;
import com.example.labequipment.entity.Device;
import com.example.labequipment.entity.RepairRecord;
import com.example.labequipment.mapper.DeviceMapper;
import com.example.labequipment.mapper.RepairRecordMapper;
import com.example.labequipment.service.IRepairService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RepairServiceImpl extends ServiceImpl<RepairRecordMapper, RepairRecord> implements IRepairService {
    private final DeviceMapper deviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reportRepair(RepairReportDTO dto) {
        Device device = deviceMapper.selectById(dto.getDeviceId());
        if(device == null) throw new RuntimeException("设备不存在");

        device.setStatus("under_repair");
        deviceMapper.updateById(device);

        RepairRecord record = new RepairRecord();
        record.setDeviceId(dto.getDeviceId());
        record.setReporterId(dto.getReporterId());
        record.setDescription(dto.getDescription());
        record.setStatus("reported");
        record.setReportedTime(LocalDateTime.now());
        this.save(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeRepair(Long repairId, String solution, BigDecimal cost) {
        RepairRecord record = this.getById(repairId);
        if(record == null) throw new RuntimeException("工单不存在");

        record.setStatus("completed");
        record.setSolution(solution);
        record.setCost(cost);
        record.setCompletedTime(LocalDateTime.now());
        this.updateById(record);

        Device device = deviceMapper.selectById(record.getDeviceId());
        device.setStatus("in_stock");
        deviceMapper.updateById(device);
    }
}
