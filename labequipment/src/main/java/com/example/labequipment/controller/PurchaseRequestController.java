package com.example.labequipment.controller;

import cn.dev33.satoken.stp.StpUtil; // 1. 引入 Sa-Token 工具类
import com.example.labequipment.common.result.Result;
import com.example.labequipment.dto.PurchaseRequestApproveDTO;
import com.example.labequipment.dto.PurchaseRequestCreateDTO;
import com.example.labequipment.entity.PurchaseRequest;
import com.example.labequipment.entity.User;
import com.example.labequipment.service.IPurchaseRequestService;
import com.example.labequipment.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/purchase-requests")
@RequiredArgsConstructor
public class PurchaseRequestController {

    private final IPurchaseRequestService purchaseRequestService;
    private final IUserService userService;

    /**
     * 提交采购申请
     * 修改点：移除 @RequestHeader，使用 StpUtil 获取 ID
     */
    @PostMapping("/submit")
    public Result<String> submitPurchaseRequest(@RequestBody @Valid PurchaseRequestCreateDTO dto) {
        // 1. 直接从 Token 获取当前登录人的 ID (安全且准确)
        long userId = StpUtil.getLoginIdAsLong();

        // 2. 获取用户信息
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }

        // 3. 权限拦截：管理员不能提交申请 (保持你的逻辑)
        if ("admin".equals(user.getRole())) {
            return Result.error(500, "管理员无需提交申请，请直接在设备管理中录入");
        }

        // 4. 业务拦截：必须绑定实验室
        // ⚠️ 注意：如果你的普通用户注册后没有分配 labId，这里会报错，导致无法申请
        if (user.getLabId() == null) {
            return Result.error(500, "您尚未加入任何实验室，无法发起采购申请");
        }

        // 5. 提交申请
        purchaseRequestService.createPurchaseRequest(dto, userId);

        return Result.success("采购申请提交成功");
    }

    /**
     * 审批采购申请
     * 修改点：移除 @RequestHeader，使用 StpUtil 获取 ID
     */
    @PostMapping("/approve")
    public Result<String> approvePurchaseRequest(@RequestBody @Valid PurchaseRequestApproveDTO dto) {
        // 1. 获取当前操作人 ID
        long currentAdminId = StpUtil.getLoginIdAsLong();

        // 2. 这里的权限校验建议放 Service 里，或者在这里查一下 User 角色是否为 admin
        // purchaseRequestService.approvePurchaseRequest 内部应该已经校验了是否为管理员

        purchaseRequestService.approvePurchaseRequest(dto, currentAdminId);

        return Result.success("审批操作成功");
    }

    /**
     * 获取采购申请列表
     */

    // 🔴 新增 1：获取采购申请列表（支持按状态筛选，如 ?status=pending）
    @GetMapping("/list")
    public Result<List<PurchaseRequest>> listPurchaseRequests(@RequestParam(required = false) String status) {
        // 这里假设你的 Service 有 list 方法，如果没有，可以用 MyBatis-Plus 的简单查询
        // 比如: return Result.success(purchaseRequestService.list());
        // 为了严谨，建议在 Service 里写一个 list(String status) 方法
        return Result.success(purchaseRequestService.listPurchaseRequests(status));
    }
    @GetMapping("/my")
    public Result<List<PurchaseRequest>> getMyPurchaseRequests() {
        // 1. 获取当前登录用户ID
        long userId = StpUtil.getLoginIdAsLong();

        // 2. 调用Service查询
        List<PurchaseRequest> list = purchaseRequestService.listMyRequests(userId);

        return Result.success(list);
    }

}