package com.example.labequipment.controller;

import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.RepairRecordDTO;
import com.example.labequipment.dto.RepairRecordVO;
import com.example.labequipment.service.IRepairService;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/repair")
@RequiredArgsConstructor
public class RepairController {
    private final IRepairService repairService;

    @PostMapping("/report")
    public Result<String> report(@RequestBody @Valid RepairRecordDTO dto) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        repairService.reportRepair(dto, currentUserId);
        return Result.success("报修已申报");
    }

    @PutMapping("/audit")
    public Result<String> audit(@RequestParam Long repairId, @RequestParam String status) {
        Long managerId = StpUtil.getLoginIdAsLong();
        repairService.auditRepair(repairId, status, managerId);
        return Result.success("安排成功");
    }

    // 对应前端的“开始维修”按钮
    @PutMapping("/start")
    public Result<String> start(@RequestParam Long repairId) {
        repairService.startRepair(repairId);
        return Result.success("设备进入维修状态");
    }

    @PostMapping("/complete")
    public Result<String> complete(@RequestParam Long repairId,
                                   @RequestParam String solution,
                                   @RequestParam BigDecimal cost,
                                   @RequestParam String outcome) { // 接收 success 或 fail
        repairService.completeRepair(repairId, solution, cost, outcome);

        String msg = "fail".equals(outcome) ? "维修已结束，已自动提交报废申请" : "维修完成，设备已回库";
        return Result.success(msg);
    }

    @GetMapping("/list")
    public Result<List<RepairRecordVO>> getRepairList(@RequestParam(required = false) String keyword) {
        return Result.success(repairService.getRepairList(keyword));
    }
}