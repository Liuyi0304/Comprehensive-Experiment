package com.example.labequipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.labequipment.dto.DeviceAddDTO;
import com.example.labequipment.dto.DeviceQueryDTO;
import com.example.labequipment.dto.DeviceTransferDTO;
import com.example.labequipment.dto.PurchaseRequestCreateDTO; // 你的采购DTO
import com.example.labequipment.entity.Device;
import java.util.List;

public interface IDeviceService extends IService<Device> {

    // === 原有业务接口 ===
    /**
     * 查询设备列表 (带权限)
     */
    List<Device> getDeviceList(DeviceQueryDTO query, Long userId);

    /**
     * 借用设备
     */
    void borrowDevice(Long deviceId, Long userId, String purpose);

    /**
     * 归还设备
     */
    void returnDevice(Long deviceId);

    /**
     * 调拨设备
     */
    void transferDevice(DeviceTransferDTO dto);

    /**
     * 新增设备 (入库)
     */
    void addDevice(DeviceAddDTO dto);

    // === 新增/补充接口 ===

    /**
     * 提交采购申请 (新增)
     */
    void submitPurchase(PurchaseRequestCreateDTO dto, Long userId);

    /**
     * 管理员直接报废 (配合Controller)
     */
    void adminDirectScrap(Long deviceId);
}