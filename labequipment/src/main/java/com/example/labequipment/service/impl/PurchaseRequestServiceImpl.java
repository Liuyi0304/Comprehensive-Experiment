package com.example.labequipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.common.exception.CustomException;
import com.example.labequipment.dto.PurchaseRequestApproveDTO;
import com.example.labequipment.dto.PurchaseRequestCreateDTO;
import com.example.labequipment.entity.Device;
import com.example.labequipment.entity.PurchaseRequest;
import com.example.labequipment.entity.User;
import com.example.labequipment.mapper.DeviceMapper;
import com.example.labequipment.mapper.PurchaseRequestMapper;
import com.example.labequipment.mapper.UserMapper;
import com.example.labequipment.service.IPurchaseRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseRequestServiceImpl extends ServiceImpl<PurchaseRequestMapper, PurchaseRequest>
        implements IPurchaseRequestService {

    private final UserMapper userMapper;
    private final DeviceMapper deviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPurchaseRequest(PurchaseRequestCreateDTO dto, Long applicantId) {
        // 1. 查询申请人
        User applicant = userMapper.selectById(applicantId);

        // 2. 权限校验
        if (applicant == null ||"admin".equals(applicant.getRole())) {
            throw new CustomException("当前身份无法提交申请");
        }

        // 3. 校验实验室
        if (applicant.getLabId() == null) {
            throw new CustomException("未绑定实验室，无法申请");
        }

        // 4. 组装数据
        PurchaseRequest request = new PurchaseRequest();
        request.setLabId(applicant.getLabId());
        request.setApplicantId(applicantId);
        request.setDeviceName(dto.getDeviceName());
        request.setCategoryId(dto.getCategoryId());
        request.setModel(dto.getModel());
        request.setManufacturer(dto.getManufacturer());
        request.setNumber(dto.getNumber());
        request.setOnePrice(dto.getOnePrice());
        request.setReason(dto.getReason());
        request.setStatus("pending");
        request.setCreatedAt(LocalDateTime.now());

        this.save(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approvePurchaseRequest(PurchaseRequestApproveDTO dto, Long adminId) {
        // 1. 查询申请单
        PurchaseRequest request = this.getById(dto.getId());
        if (request == null) {
            throw new CustomException("采购申请不存在");
        }
        if (!"pending".equals(request.getStatus())) {
            throw new CustomException("申请已处理");
        }

        // 2. 校验管理员
        User admin = userMapper.selectById(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            throw new CustomException("无权审批");
        }

        // 3. 审批逻辑
        String actionStatus = dto.getStatus(); // 获取 approved 或 rejected

        if ("approved".equals(actionStatus)) {
            request.setStatus("approved");

            // === 自动入库逻辑 ===
            for (int i = 0; i < request.getNumber(); i++) {

                Device device = new Device();
                device.setAssetNumber("CG" + System.currentTimeMillis());
                device.setName(request.getDeviceName());
                device.setModel(request.getModel());
                device.setManufacturer(request.getManufacturer());
                device.setLabId(request.getLabId());
                device.setCategoryId(request.getCategoryId());
                device.setStatus("in_stock");
                device.setPrice(request.getOnePrice());
                device.setCreatedAt(LocalDateTime.now());

                deviceMapper.insert(device);
            }
        } else if ("rejected".equals(actionStatus)) {
            request.setStatus("rejected");
            // 拒绝逻辑：只改状态
        } else {
            throw new CustomException("无效的审批状态");
        }

        // 4. 更新申请单
        request.setApprovedBy(adminId);
        request.setApprovedAt(LocalDateTime.now());

        this.updateById(request);
    }

    @Override
    public List<PurchaseRequest> listPurchaseRequests(String status) {
        LambdaQueryWrapper<PurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(status)) {
            wrapper.eq(PurchaseRequest::getStatus, status);
        }
        wrapper.orderByDesc(PurchaseRequest::getCreatedAt);
        return this.list(wrapper);
    }
    /**
     * ✅ 新增：查看“我的申请”列表
     */
    @Override
    public List<PurchaseRequest> listMyRequests(Long userId) {
        LambdaQueryWrapper<PurchaseRequest> wrapper = new LambdaQueryWrapper<>();
        // 只查询申请人ID等于当前用户ID的记录
        wrapper.eq(PurchaseRequest::getApplicantId, userId);
        // 按时间倒序
        wrapper.orderByDesc(PurchaseRequest::getCreatedAt);
        return this.list(wrapper);
    }
}