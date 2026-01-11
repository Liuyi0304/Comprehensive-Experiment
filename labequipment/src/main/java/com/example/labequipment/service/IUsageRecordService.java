package com.example.labequipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.labequipment.dto.UsageRequestDTO;
import com.example.labequipment.entity.UsageRecord;

import java.util.List;

public interface IUsageRecordService extends IService<UsageRecord> {
    // 开始使用登记
    void startUsage(UsageRequestDTO dto, Long userId);
    // 结束使用（归还）
    void endUsage(Long recordId, Long userId);
    // 获取使用列表
    List<UsageRecord> getUsageList(String keyword);

    void endUsageByDevice(Long deviceId, Long userId);
}