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

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class UsageRecordController {

    private final IUsageRecordService usageService;

    /**
     * 1. 登记开始使用 (仅 user 角色)
     * 前端调用: POST /api/usage/start
     */
    @PostMapping("/start")
    public Result<String> startUsage(@RequestBody @Valid UsageRequestDTO dto) {
        // 从 Sa-Token 获取当前登录的人员 ID
        Long userId = StpUtil.getLoginIdAsLong();
        usageService.startUsage(dto, userId);
        return Result.success("登记成功，设备状态已更新为：在用");
    }

    /**
     * 2. 归还设备/结束使用
     * 前端调用: PUT /api/usage/end?recordId=xxx
     */
    @PutMapping("/end")
    public Result<String> endUsage(@RequestParam Long recordId) {
        Long userId = StpUtil.getLoginIdAsLong();
        usageService.endUsage(recordId, userId);
        return Result.success("归还成功，设备已回库");
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