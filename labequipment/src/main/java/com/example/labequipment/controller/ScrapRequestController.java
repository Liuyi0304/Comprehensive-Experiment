package com.example.labequipment.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.ScrapRequestApproveDTO;
import com.example.labequipment.dto.ScrapRequestCreateDTO;
import com.example.labequipment.entity.ScrapRequest;
import com.example.labequipment.service.IScrapRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scrap-requests")
@RequiredArgsConstructor
public class ScrapRequestController {

    private final IScrapRequestService scrapRequestService;

    /**
     * 1. 提交报废申请 (Manager)
     */
    @PostMapping("/submit")
    public Result<String> createScrapRequest(@RequestBody ScrapRequestCreateDTO dto) {
        scrapRequestService.createScrapRequest(dto, StpUtil.getLoginIdAsLong());
        return Result.success("报废申请已提交");
    }

    /**
     * 2. 审批报废申请 (Admin)
     */
    @PostMapping("/approve")
    public Result<String> approveScrapRequest(@RequestBody ScrapRequestApproveDTO dto) {
        scrapRequestService.approveScrapRequest(dto, StpUtil.getLoginIdAsLong());
        return Result.success("审批完成");
    }

    /**
     * 3. 获取我的报废申请 (Manager 查看历史记录)
     */
    @GetMapping("/my")
    public Result<List<ScrapRequest>> getMyScrapRequests() {
        // 1. 获取当前登录用户的 ID
        long userId = StpUtil.getLoginIdAsLong();

        // 2. 调用 Service 查询该用户的申请
        // (如果 Service 还没这个方法，看下一步)
        return Result.success(scrapRequestService.listMyScrapRequests(userId));
    }
    /**
     * 4. 获取所有报废申请 (Admin 审批页面用)
     */
    @GetMapping("/all")
    public Result<List<ScrapRequest>> getAllScrapRequests(@RequestParam(required = false) String status) {
        return Result.success(scrapRequestService.listAllScrapRequests(status));
    }

}