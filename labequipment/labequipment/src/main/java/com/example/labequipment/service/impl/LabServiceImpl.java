package com.example.labequipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.labequipment.common.exception.CustomException; // 假设你有自定义异常
import com.example.labequipment.dto.LabAddDTO;
import com.example.labequipment.entity.Lab;
import com.example.labequipment.mapper.LabMapper;
import com.example.labequipment.service.ILabService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LabServiceImpl implements ILabService {

    private final LabMapper labMapper;

    @Override
    public List<Lab> list() {
        // 直接查询所有，按照 ID 排序（可选）
        return labMapper.selectList(new LambdaQueryWrapper<Lab>().orderByAsc(Lab::getId));
    }

    @Override
    public void addLab(LabAddDTO dto) {
        // 1. 校验名称是否重复
        LambdaQueryWrapper<Lab> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Lab::getName, dto.getName());
        if (labMapper.selectCount(wrapper) > 0) {
            throw new CustomException("实验室名称 [" + dto.getName() + "] 已存在");
        }

        // 2. 数据转换
        Lab lab = new Lab();
        BeanUtils.copyProperties(dto, lab);

        // 3. 填充创建时间
        lab.setCreatedTime(LocalDateTime.now());

        // 4. 插入数据库
        labMapper.insert(lab);
    }
}