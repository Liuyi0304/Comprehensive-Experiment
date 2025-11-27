package com.example.labequipment.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.labequipment.dto.DeviceTransferDTO;
import com.example.labequipment.entity.Device;

public interface IDeviceService extends IService<Device> {
    void borrowDevice(Long deviceId, Long userId, String purpose);
    void returnDevice(Long deviceId);
    void transferDevice(DeviceTransferDTO dto);
}
