// com/example/labequipment/service/IPurchaseRequestService.java
package com.example.labequipment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.labequipment.dto.PurchaseRequestApproveDTO;
import com.example.labequipment.dto.PurchaseRequestCreateDTO;
import com.example.labequipment.entity.PurchaseRequest;
import java.util.List;

public interface IPurchaseRequestService extends IService<PurchaseRequest> {

    void createPurchaseRequest(PurchaseRequestCreateDTO dto, Long applicantId);

    void approvePurchaseRequest(PurchaseRequestApproveDTO dto, Long adminId);
    List<PurchaseRequest> listMyRequests(Long userId);
    List<PurchaseRequest> listPurchaseRequests(String status);
}