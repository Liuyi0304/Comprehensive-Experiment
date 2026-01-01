package com.example.labequipment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils; // 或者用 org.apache.commons.lang3.StringUtils
import com.example.labequipment.common.exception.CustomException;
import com.example.labequipment.dto.LabAddDTO; // 确认包名是否正确
import com.example.labequipment.entity.Lab;
import com.example.labequipment.entity.User;
import com.example.labequipment.mapper.LabMapper;
import com.example.labequipment.mapper.UserMapper;
import com.example.labequipment.service.ILabService;
import com.example.labequipment.dto.LabVO; // 确认包名是否正确
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabServiceImpl implements ILabService {

    private final LabMapper labMapper;
    private final UserMapper userMapper; // ✅ 必须注入 UserMapper 才能查人名

    // 1. 核心查询：返回 LabVO（包含负责人姓名）
    @Override
    public List<LabVO> list(String keyword) {
        LambdaQueryWrapper<Lab> wrapper = new LambdaQueryWrapper<>();

        // --- 🔍 智能搜索逻辑：搜名字、搜位置、搜负责人姓名 ---
        if (StringUtils.isNotBlank(keyword)) {
            // A. 先去用户表找出名字包含 keyword 的人，拿到他们的 ID
            List<Long> matchedUserIds = userMapper.selectList(
                    new LambdaQueryWrapper<User>().like(User::getRealName, keyword)
            ).stream().map(User::getId).collect(Collectors.toList());

            // B. 组合查询条件
            wrapper.and(w -> {
                // 1. 搜实验室名 或 位置
                w.like(Lab::getName, keyword)
                        .or().like(Lab::getLocation, keyword);

                // 2. 如果搜到了对应名字的人，就把他们管理的实验室也查出来
                if (!matchedUserIds.isEmpty()) {
                    w.or().in(Lab::getManagerId, matchedUserIds);
                }
            });
        }

        // 排序：按 ID 倒序
        wrapper.orderByDesc(Lab::getId);

        // 执行查询
        List<Lab> labs = labMapper.selectList(wrapper);
        if (labs.isEmpty()) {
            return Collections.emptyList();
        }

        // --- 🔄 转换为 VO 并填充负责人姓名 ---
        // 1. 提取所有出现的 managerId（去重，避免重复查库）
        List<Long> userIds = labs.stream()
                .map(Lab::getManagerId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        // 2. 批量查出这些用户
        List<User> managers = userIds.isEmpty() ? Collections.emptyList() : userMapper.selectBatchIds(userIds);

        // 3. 转换并组装
        return labs.stream().map(lab -> {
            LabVO vo = new LabVO();
            BeanUtils.copyProperties(lab, vo); // 复制基础属性

            // 匹配姓名
            if (lab.getManagerId() != null) {
                managers.stream()
                        .filter(u -> u.getId().equals(lab.getManagerId()))
                        .findFirst()
                        .ifPresent(u -> vo.setManagerRealName(u.getRealName()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    // 2. 更新方法
    @Override
    public boolean updateById(Lab lab) {
        if (lab.getId() == null) {
            throw new CustomException("更新失败：ID不能为空");
        }
        // 使用 updateById 会自动根据 ID 更新非空字段
        // 前端传来的 DTO 转换成 Lab 实体后，managerId 只要有值就会被更新
        return labMapper.updateById(lab) > 0;
    }

    // 3. 新增方法
    @Override
    public void addLab(LabAddDTO dto) {
        // 查重逻辑
        Long count = labMapper.selectCount(new LambdaQueryWrapper<Lab>().eq(Lab::getName, dto.getName()));
        if (count > 0) {
            throw new CustomException("实验室名称 [" + dto.getName() + "] 已存在");
        }

        Lab lab = new Lab();
        BeanUtils.copyProperties(dto, lab);

        // 如果有创建时间字段，可以在这里自动填充，或者由数据库默认值处理
        // lab.setCreatedTime(LocalDateTime.now());

        labMapper.insert(lab);
    }

    // 4. 删除方法
    @Override
    public boolean removeById(Long id) {
        // 这里后续可以加入校验：例如"如果该实验室还有设备，禁止删除"
        return labMapper.deleteById(id) > 0;
    }

    @Override
    public Lab getById(Long id) {
        return labMapper.selectById(id);
    }
}