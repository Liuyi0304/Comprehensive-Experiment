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
import java.util.Map;

@RestController
@RequestMapping("/weixinapi/scrap")
@RequiredArgsConstructor
public class WScrapRequestController {

    private final IScrapRequestService scrapRequestService;
    /**
     * 1. 提交报废申请 (Manager)
     */
    @PostMapping("/submit")
    public Result<String> createScrapRequest(@RequestBody Map<String, Object> params) {
        // 1. 从 map 中手动提取字段
        Long deviceId = Long.valueOf(params.get("deviceId").toString());
        String reason = (String) params.get("reason");

        // 2. 直接提取前端传来的 applicantId
        Long applicantId = Long.valueOf(params.get("applicantId").toString());

        // 3. 构造 DTO 传给 Service (或者直接传参数给 Service)
        ScrapRequestCreateDTO dto = new ScrapRequestCreateDTO();
        dto.setDeviceId(deviceId);
        dto.setReason(reason);

        scrapRequestService.createScrapRequest(dto, applicantId);

        return Result.success("报废申请已提交");
    }

    /**
     * 2. 审批报废申请 (Admin)
     */
    @PostMapping("/approve")
    public Result<String> approveScrapRequest(@RequestBody Map<String, Object> params) {
        // 1. 手动从 Map 中提取 DTO 需要的字段并组装
        ScrapRequestApproveDTO dto = new ScrapRequestApproveDTO();
        dto.setRequestId(Long.valueOf(params.get("requestId").toString()));
        dto.setStatus((String) params.get("status"));
        dto.setRejectedReason((String) params.get("rejectedReason"));

        // 2. 从 Map 中提取 DTO 里没有的 operatorId
        Long operatorId = Long.valueOf(params.get("operatorId").toString());

        // 3. 调用 Service
        scrapRequestService.approveScrapRequest(dto, operatorId);

        return Result.success("审批完成");
    }

    /**
     * 2. 拒绝报废申请 (Admin)
     */
    @PostMapping("/reject")
    public Result<String> rejectScrapRequest(@RequestBody Map<String, Object> params) {
        // 1. 手动组装 DTO (不改变你现有的 DTO 类结构)
        ScrapRequestApproveDTO dto = new ScrapRequestApproveDTO();

        // 安全地将前端传来的 requestId 转为 Long
        if (params.get("requestId") != null) {
            dto.setRequestId(Long.valueOf(params.get("requestId").toString()));
        }

        // 设置状态和驳回原因
        dto.setStatus((String) params.get("status")); // 值为 'rejected'
        dto.setRejectedReason((String) params.get("rejectedReason"));

        // 2. 提取 DTO 之外的审批人 ID
        Long operatorId = null;
        if (params.get("operatorId") != null) {
            operatorId = Long.valueOf(params.get("operatorId").toString());
        }

        // 3. 调用 Service 执行业务逻辑
        // 即使是拒绝，通常也调用同一个 service 方法，内部根据 dto.getStatus() 分支处理
        scrapRequestService.approveScrapRequest(dto, operatorId);
        return Result.success("申请已成功驳回");
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