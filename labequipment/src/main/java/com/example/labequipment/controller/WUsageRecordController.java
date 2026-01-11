package com.example.labequipment.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.UsageRequestDTO;
import com.example.labequipment.entity.UsageRecord;
import com.example.labequipment.service.IUsageRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/weixinapi/usage")
@RequiredArgsConstructor
public class WUsageRecordController {

    private final IUsageRecordService usageService;

    /**
     * 1. 登记开始使用 (仅 user 角色)
     * 前端调用: POST /api/usage/start
     */
    @PostMapping("/start")
    public Result<String> startUsage(@RequestBody Map<String, Object> params) {
        // 1. 手动组装 DTO (不改变你现有的 DTO 类结构)
        UsageRequestDTO dto = new UsageRequestDTO();

        // 安全转换 deviceId
        if (params.get("deviceId") != null) {
            dto.setDeviceId(Long.valueOf(params.get("deviceId").toString()));
        }

        // 获取用途 purpose
        dto.setPurpose((String) params.get("purpose"));

        // 2. 提取 DTO 之外的人员 ID (userId)
        Long userId = null;
        if (params.get("userId") != null) {
            userId = Long.valueOf(params.get("userId").toString());
        } else {
            return Result.error(500,"用户ID不能为空");
        }

        // 3. 调用 Service
        usageService.startUsage(dto, userId);

        return Result.success("登记成功，设备状态已更新为：在用");
    }

    /**
     * 2. 归还设备/结束使用
     * 前端调用: PUT /api/usage/end?recordId=xxx
     */
    @PostMapping("/end")
    public Result<String> endUsage(@RequestBody Map<String, Object> params) {
        Long deviceId = Long.valueOf(params.get("deviceId").toString());
        Long userId = Long.valueOf(params.get("userId").toString());

        // 调用新写的 Service 方法
        usageService.endUsageByDevice(deviceId, userId);

        return Result.success("归还成功");
    }
    /**
     * 3. 获取领用记录列表
     * 前端调用: GET /api/usage/list?keyword=xxx
     */
    @GetMapping("/list")
    public Result<List<UsageRecord>> getList(@RequestParam(required = false) String keyword) {
        List<UsageRecord> list = usageService.getUsageList(keyword);
        return Result.success(list);
    }
}