package com.example.labequipment.controller;
import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.DeviceTransferDTO;
import com.example.labequipment.entity.Device;
import com.example.labequipment.service.IDeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/device")
@RequiredArgsConstructor
public class DeviceController {
    private final IDeviceService deviceService;

    @GetMapping("/list")
    public Result<List<Device>> list() {
        return Result.success(deviceService.list());
    }

    @PostMapping("/transfer")
    public Result<String> transfer(@RequestBody @Valid DeviceTransferDTO dto) {
        deviceService.transferDevice(dto);
        return Result.success("调拨成功");
    }

    @PostMapping("/borrow")
    public Result<String> borrow(@RequestParam Long deviceId,
                                 @RequestParam Long userId,
                                 @RequestParam String purpose) {
        deviceService.borrowDevice(deviceId, userId, purpose);
        return Result.success("借用成功");
    }

    @PostMapping("/return")
    public Result<String> returnDevice(@RequestParam Long deviceId) {
        deviceService.returnDevice(deviceId);
        return Result.success("归还成功");
    }
}
