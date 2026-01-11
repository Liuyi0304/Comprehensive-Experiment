package com.example.labequipment.controller;

import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.RepairRecordDTO;
import com.example.labequipment.dto.RepairRecordVO;
import com.example.labequipment.entity.RepairRecord;
import com.example.labequipment.service.IRepairService;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/weixinapi/repair")
@RequiredArgsConstructor
public class WRepairController {
    private final IRepairService repairService;

    @PostMapping("/report")
    public Result<String> report(@RequestBody @Valid RepairRecordDTO dto) {
        // 逻辑：如果前端传了 reporterId 就用前端的，没传就用 StpUtil 解析的
        Long finalUserId = dto.getReporterId();
        // 调用 service
        repairService.reportRepair(dto, finalUserId);
        return Result.success("报修已申报");
    }

    @PostMapping("/assign")
    public Result<String> assign(@RequestBody Map<String, Object> params) {
        // 1. 从前端传输的 JSON 中提取参数
        // 注意：前端传的是 id 和 handlerId
        Long repairId = Long.valueOf(params.get("id").toString());
        Long handlerId = Long.valueOf(params.get("handlerId").toString());
        String status = params.get("status").toString();

        // 2. 调用 service，不再使用 StpUtil 获取 ID
        repairService.auditRepair(repairId, status, handlerId);

        return Result.success("指派成功");
    }

    // 对应前端的“开始维修”按钮
    @PostMapping("/start")
    public Result<String> start(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        repairService.startRepair(id);
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
    public Result<List<RepairRecordVO>> getRepairList(
            @RequestParam Long userId,        // 接收前端传来的 userId
            @RequestParam(required = false) String keyword) {

        // 将 userId 传给 Service，Service 内部就可以直接用这个 ID 做逻辑了
        List<RepairRecordVO> list = repairService.getWRepairList(userId, keyword);
        return Result.success(list);
    }

    /**
     * 获取指定处理人的待处理和维修中任务
     */
    @GetMapping("/listTasks")
    public Result<List<RepairRecord>> listTasks(@RequestParam Long handlerId) {
        // 1. 调用 Service 获取数据
        List<RepairRecord> tasks = repairService.getTasksByHandler(handlerId);
        return Result.success(tasks);
    }
}