package com.example.labequipment.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.labequipment.dto.RepairReportDTO;
import com.example.labequipment.entity.RepairRecord;
import java.math.BigDecimal;

public interface IRepairService extends IService<RepairRecord> {
    void reportRepair(RepairReportDTO dto);
    void completeRepair(Long repairId, String solution, BigDecimal cost);
}
