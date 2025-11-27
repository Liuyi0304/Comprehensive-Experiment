package com.example.labequipment.controller;
import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.RepairReportDTO;
import com.example.labequipment.service.IRepairService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/repair")
@RequiredArgsConstructor
public class RepairController {
    private final IRepairService repairService;

    @PostMapping("/report")
    public Result<String> report(@RequestBody @Valid RepairReportDTO dto) {
        repairService.reportRepair(dto);
        return Result.success("报修成功");
    }

    @PostMapping("/complete")
    public Result<String> complete(@RequestParam Long repairId,
                                   @RequestParam String solution,
                                   @RequestParam BigDecimal cost) {
        repairService.completeRepair(repairId, solution, cost);
        return Result.success("维修完成");
    }
}
