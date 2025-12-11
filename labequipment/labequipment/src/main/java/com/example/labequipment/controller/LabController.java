package com.example.labequipment.controller;

import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.LabAddDTO;
import com.example.labequipment.entity.Lab;
import com.example.labequipment.service.ILabService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab")
@RequiredArgsConstructor
public class LabController {

    private final ILabService labService;

    /**
     * 获取实验室列表
     * GET /api/lab/list
     */
    @GetMapping("/list")
    public Result<List<Lab>> list() {
        return Result.success(labService.list());
    }

    /**
     * 新增实验室
     * POST /api/lab/add
     */
    @PostMapping("/add")
    public Result<String> addLab(@RequestBody @Valid LabAddDTO dto) {
        labService.addLab(dto);
        return Result.success("实验室创建成功");
    }
}