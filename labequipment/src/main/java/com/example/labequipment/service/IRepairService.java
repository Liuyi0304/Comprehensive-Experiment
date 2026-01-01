package com.example.labequipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.labequipment.dto.RepairRecordDTO;
import com.example.labequipment.dto.RepairRecordVO;
import com.example.labequipment.entity.RepairRecord;
import java.math.BigDecimal;
import java.util.List;

public interface IRepairService extends IService<RepairRecord> {
    // 1. 发起报修 (状态: reported)
    void reportRepair(RepairRecordDTO dto, Long userId);

    // 2. 管理员安排 (状态: assigned)
    void auditRepair(Long repairId, String status, Long managerId);

    // 3. 开始维修 (状态: in_progress, 设备状态: under_repair)
    void startRepair(Long repairId);

    // 4. 维修完成 (状态: completed, 设备状态: in_stock)
    void completeRepair(Long repairId, String solution, BigDecimal cost, String outcome);
    // 列表查询
    List<RepairRecordVO> getRepairList(String keyword);
}