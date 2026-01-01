package com.example.labequipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.common.exception.CustomException;
import com.example.labequipment.dto.ScrapRequestApproveDTO;
import com.example.labequipment.dto.ScrapRequestCreateDTO;
import com.example.labequipment.entity.Device;
import com.example.labequipment.entity.ScrapRequest;
import com.example.labequipment.entity.User;
import com.example.labequipment.mapper.DeviceMapper;
import com.example.labequipment.mapper.ScrapRequestMapper;
import com.example.labequipment.mapper.UserMapper;
import com.example.labequipment.service.IScrapRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScrapRequestServiceImpl extends ServiceImpl<ScrapRequestMapper, ScrapRequest>
        implements IScrapRequestService {

    private final DeviceMapper deviceMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createScrapRequest(ScrapRequestCreateDTO dto, Long userId) {
        // 1. 校验设备
        Device device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) {
            throw new CustomException("设备不存在");
        }
        if ("scrapped".equals(device.getStatus())) {
            throw new CustomException("该设备已报废，无需重复申请");
        }

        // 2. 防止重复提交 (检查是否有 pending 状态的申请)
        Long count = this.lambdaQuery()
                .eq(ScrapRequest::getDeviceId, dto.getDeviceId())
                .eq(ScrapRequest::getStatus, "pending")
                .count();
        if (count > 0) {
            throw new CustomException("该设备已有正在处理的报废申请");
        }

        // 3. 创建申请
        ScrapRequest request = new ScrapRequest();
        request.setDeviceId(dto.getDeviceId());
        request.setApplicantId(userId);
        request.setReason(dto.getReason());
        request.setStatus("pending");
        request.setCreatedAt(LocalDateTime.now());

        this.save(request);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveScrapRequest(ScrapRequestApproveDTO dto, Long adminId) {
        // 1. 查询申请单
        ScrapRequest request = this.getById(dto.getRequestId());
        if (request == null) throw new CustomException("申请单不存在");
        if (!"pending".equals(request.getStatus())) throw new CustomException("该申请已处理");

        // 2. 校验管理员权限
        User admin = userMapper.selectById(adminId);
        if (admin == null || !"admin".equals(admin.getRole())) {
            throw new CustomException("无权审批");
        }

        // 3. 处理审批结果
        if ("approved".equals(dto.getStatus())) {
            // === 通过 ===
            request.setStatus("approved");

            // 核心操作：将设备状态改为 'scrapped'
            Device device = deviceMapper.selectById(request.getDeviceId());
            if (device != null) {
                device.setStatus("scrapped");
                // 可以在这里记录设备表的 scrapped_at 时间，如果设备表有这个字段
                // device.setScrappedAt(LocalDateTime.now());
                deviceMapper.updateById(device);
            }

        } else if ("rejected".equals(dto.getStatus())) {
            // === 驳回 ===
            request.setStatus("rejected");
            // 记录驳回理由
            request.setRejectedReason(dto.getRejectedReason());
        } else {
            throw new CustomException("无效的状态");
        }

        // 4. 更新申请单信息
        request.setApprovedBy(adminId);
        request.setApprovedAt(LocalDateTime.now());

        this.updateById(request);
    }

    @Override
    public List<ScrapRequest> listMyScrapRequests(Long userId) {
        List<ScrapRequest> list = this.lambdaQuery()
                .eq(ScrapRequest::getApplicantId, userId)
                .orderByDesc(ScrapRequest::getCreatedAt)
                .list();

        // 填充设备信息供前端展示
        populateDetails(list);
        return list;
    }

    @Override
    public List<ScrapRequest> listAllScrapRequests(String status) {
        LambdaQueryWrapper<ScrapRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ScrapRequest::getStatus, status);
        }
        wrapper.orderByDesc(ScrapRequest::getCreatedAt);

        List<ScrapRequest> list = this.list(wrapper);
        populateDetails(list);
        return list;
    }

    // 辅助方法：填充设备名称和申请人姓名
    private void populateDetails(List<ScrapRequest> list) {
        for (ScrapRequest req : list) {
            // 查设备名
            Device d = deviceMapper.selectById(req.getDeviceId());
            if (d != null) {
                req.setDeviceName(d.getName());
                req.setDeviceAssetNumber(d.getAssetNumber());
            }
            // 查申请人名
            User u = userMapper.selectById(req.getApplicantId());
            if (u != null) {
                // 如果User表有realName用realName，没有用username
                req.setApplicantName(u.getUsername());
            }
        }
    }
}