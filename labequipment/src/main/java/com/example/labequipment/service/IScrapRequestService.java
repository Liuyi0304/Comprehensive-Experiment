package com.example.labequipment.service;

import com.example.labequipment.dto.ScrapRequestApproveDTO;
import com.example.labequipment.dto.ScrapRequestCreateDTO;
import com.example.labequipment.entity.ScrapRequest;
import java.util.List;

public interface IScrapRequestService {
    // 实验室负责人提交报废申请
    void createScrapRequest(ScrapRequestCreateDTO dto, Long currentUserId);

    // 管理员审批报废申请
    void approveScrapRequest(ScrapRequestApproveDTO dto, Long adminId);

    // 查询我的申请
    List<ScrapRequest> listMyScrapRequests(Long userId);

    // 查询所有申请（管理员）
    List<ScrapRequest> listAllScrapRequests(String status);
}