package com.example.labequipment.controller;

import cn.dev33.satoken.stp.StpUtil; // 👈 必须导入这个，用来获取当前登录人ID
import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.DeviceAddDTO;
import com.example.labequipment.dto.DeviceQueryDTO; // 👈 导入查询DTO
import com.example.labequipment.dto.DeviceTransferDTO;
import com.example.labequipment.entity.Device;
import com.example.labequipment.mapper.DeviceMapper;
import com.example.labequipment.service.IDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeviceController {

    private final IDeviceService deviceService;
    private final DeviceMapper deviceMapper;

    // ================== 基础设备接口 ==================

    /**
     * ✅ 修正：查询设备列表
     * 1. 接收 DeviceQueryDTO (前端传来的搜索条件)
     * 2. 获取当前 userId
     * 3. 调用 service.getDeviceList (走权限判断逻辑)
     */
    @GetMapping("/devices")
    public Result<List<Device>> list(DeviceQueryDTO query) {
        // 获取当前登录用户的 ID
        long userId = StpUtil.getLoginIdAsLong();

        // 调用我们在 Service 里写的带权限控制的方法
        return Result.success(deviceService.getDeviceList(query, userId));
    }

    @PostMapping("/devices/add")
    public Result<String> addDevice(@RequestBody @Valid DeviceAddDTO dto) {
        deviceService.addDevice(dto);
        return Result.success("设备录入成功");
    }

    @PostMapping("/devices/transfer")
    public Result<String> transfer(@RequestBody @Valid DeviceTransferDTO dto) {
        deviceService.transferDevice(dto);
        return Result.success("调拨成功");
    }

    // ================== 管理员专用接口 ==================

    @PostMapping("/admin/device/scrap")
    public Result<?> adminDirectScrap(@RequestBody Map<String, Object> params) {
        Object deviceIdObj = params.get("deviceId");
        if (deviceIdObj == null) {
            return Result.error(500, "设备ID不能为空");
        }

        Long deviceId = Long.valueOf(deviceIdObj.toString());
        Device device = deviceMapper.selectById(deviceId);
        if (device == null) {
            return Result.error(500, "设备不存在");
        }

        if ("scrapped".equals(device.getStatus())) {
            return Result.error(500, "该设备已经是报废状态");
        }

        try {
            device.setStatus("scrapped");
            device.setScrappedAt(LocalDateTime.now());
            deviceMapper.updateById(device);
            log.info("管理员直接报废设备: ID={}, Name={}", device.getId(), device.getName());
            return Result.success("设备已直接报废");
        } catch (Exception e) {
            log.error("报废操作失败", e);
            return Result.error(500, "操作失败：" + e.getMessage());
        }
    }
}