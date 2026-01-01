package com.example.labequipment.service;

import com.example.labequipment.dto.LabAddDTO;
import com.example.labequipment.dto.LabVO;
import com.example.labequipment.entity.Lab;

import java.util.List;

    public interface ILabService {

        // ✅ 修改这里：增加 keyword 参数
        List<LabVO> list(String keyword);

        // ✅ 保持这里：用 Lab 实体接收
        boolean updateById(Lab lab);

        // ... 其他 addLab, delete 等保持原样
        void addLab(LabAddDTO dto);
        boolean removeById(Long id);
        Lab getById(Long id);
}