package com.example.labequipment.dto;

import lombok.Data;

@Data
public class DeviceQueryDTO {
    /**
     * 搜索关键词 (设备名称 或 资产编号)
     */
    private String name;

    /**
     * 设备状态 (in_stock, in_use, etc.)
     */
    private String status;

    /**
     * 实验室ID (管理员筛选用，普通用户传了也没用，后端会覆盖)
     */
    private Long labId;
}