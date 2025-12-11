package com.example.labequipment.service;

import com.example.labequipment.dto.LabAddDTO;
import com.example.labequipment.entity.Lab;
import java.util.List;

public interface ILabService {
    /**
     * 获取所有实验室列表
     */
    List<Lab> list();

    /**
     * 新增实验室
     */
    void addLab(LabAddDTO dto);
}