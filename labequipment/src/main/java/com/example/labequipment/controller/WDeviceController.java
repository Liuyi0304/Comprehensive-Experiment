package com.example.labequipment.controller;
import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.DeviceTransferDTO;
import com.example.labequipment.entity.Device;
import com.example.labequipment.service.IDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/weixinapi/devices")
@RequiredArgsConstructor
public class WDeviceController {
    private final IDeviceService deviceService;

    @GetMapping("/all")
    public Result<List<Device>> list() {
        return Result.success(deviceService.list());
    }

    @GetMapping("/delete") // 路径里不再有 {id}
    public Result deleteDevice(@RequestParam("id") Long id) { // 使用 @RequestParam
        // 调试打印
        System.out.println("收到查询参数 ID: " + id);

        if (id == null) return Result.error(500,"ID不能为空");

        boolean success = deviceService.removeById(id);
        return success ? Result.success("删除成功") : Result.error(500,"删除失败");
    }

    @PostMapping("/add")
    public Result add(@RequestBody Device device) {
        // 设置创建时间
        device.setCreatedAt(LocalDateTime.now());

        // 如果没有传入状态，默认为在库
        if (device.getStatus() == null) {
            device.setStatus("in_stock");
        }

        boolean success = deviceService.save(device);
        if (success) {
            return Result.success("设备入库成功");
        }
        return Result.error(500,"入库失败，请检查资产编号是否重复");
    }

    @PostMapping("/update")
    public Result update(@RequestBody Device device) {
        // 前端必须传回 ID
        if (device.getId() == null) {
            return Result.error(500,"更新失败：设备ID不能为空");
        }

        // 处理报废逻辑：如果状态改为 scrapped，记录报废时间
        if ("scrapped".equals(device.getStatus())) {
            device.setScrappedAt(LocalDateTime.now());
        }

        // updateById 会根据实体类中的 ID 自动更新其他非空字段
        boolean success = deviceService.updateById(device);
        if (success) {
            return Result.success("档案更新成功");
        }
        return Result.error(500,"更新失败，设备可能已被删除");
    }

    @GetMapping("/detail")
    public Result getDetail(@RequestParam Long id) {
        Device device = deviceService.getById(id);
        if (device != null) {
            return Result.success(device);
        }
        return Result.error(500,"未找到该设备信息");
    }

    @GetMapping("/listByLab")
    public Result listByLab(@RequestParam("labId") Long labId) {
        try {
            // 1. 调用 Service 层查询该实验室下且未报废的所有设备
            List<Device> deviceList = deviceService.findDevicesByLabId(labId);

            // 2. 返回统一的结果对象 (code: 200, data: list)
            return Result.success(deviceList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(500,"服务器获取设备列表失败");
        }
    }

    @GetMapping("/getManager")
    public Result<Long> getManagerByDevice(@RequestParam Long deviceId) {
        // 调用 Service 获取该设备所属实验室的管理员 ID
        Long managerId = deviceService.findManagerIdByDeviceId(deviceId);

        if (managerId == null) {
            return Result.error(500,"未找到该设备对应的实验室负责人");
        }

        return Result.success(managerId);
    }

}
