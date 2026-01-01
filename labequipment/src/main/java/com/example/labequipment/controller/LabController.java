package com.example.labequipment.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.LabVO;
import com.example.labequipment.entity.Device;
import com.example.labequipment.entity.Lab;
import com.example.labequipment.dto.LabAddDTO; // ⚠️如果报错，请检查是 .dto 还是 .entity.dto
import com.example.labequipment.service.IDeviceService;
import com.example.labequipment.service.ILabService;
import jakarta.validation.Valid; // ✅ 补上这个 Valid 校验包
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab")
@RequiredArgsConstructor // ✅ 使用 Lombok 自动生成构造函数注入，非常规范
public class LabController {

    private final ILabService labService;
    private final IDeviceService deviceService; // ✅ 这里的设备服务非常关键，用于删除校验

    /**
     * 1. 列表查询（支持关键字搜索）
     * GET /lab/list?keyword=xxx
     */
    @GetMapping("/list")
    public Result<List<LabVO>> list(@RequestParam(required = false) String keyword) {
        // 调用 Service 层修改后的带参数查询方法
        return Result.success(labService.list(keyword));
    }

    /**
     * 2. 新增实验室
     * POST /lab/add
     */
    @PostMapping("/add")
    public Result<String> addLab(@RequestBody @Valid LabAddDTO dto) {
        labService.addLab(dto);
        return Result.success("实验室创建成功");
    }

    /**
     * 3. 修改实验室
     * PUT /lab/update
     */
    @PutMapping("/update")
    public Result<String> updateLab(@RequestBody Lab lab) {
        boolean success = labService.updateById(lab);
        if (success) {
            return Result.success("修改成功");
        }
        return Result.error(500, "修改失败，实验室可能不存在");
    }

    /**
     * 4. 删除实验室（带核心保护机制）
     * DELETE /lab/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> deleteLab(@PathVariable Long id) {
        // --- 🛡️ 核心保护逻辑 (我刚才不该删的！) ---
        // 检查该实验室内是否有设备
        long deviceCount = deviceService.count(
                new LambdaQueryWrapper<Device>().eq(Device::getLabId, id)
        );

        if (deviceCount > 0) {
            // 如果有设备，严禁删除，返回明确提示
            return Result.error(500, "删除失败：该实验室名下还有 " + deviceCount + " 台设备！\n请先前往【设备管理】将它们转移或报废，才能删除该实验室。");
        }
        // --- 保护逻辑结束 ---

        // 只有没设备时，才真正删除
        boolean success = labService.removeById(id);
        if (success) {
            return Result.success("删除成功");
        }
        return Result.error(500, "删除失败，数据可能已被删除");
    }

    /**
     * 5. 获取详情
     * GET /lab/get/{id}
     */
    @GetMapping("/get/{id}")
    public Result<Lab> getById(@PathVariable Long id) {
        return Result.success(labService.getById(id));
    }
}