package com.example.labequipment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.labequipment.entity.Category;
import com.example.labequipment.mapper.CategoryMapper;
import com.example.labequipment.service.ICategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
    // MyBatis-Plus 会自动处理大部分逻辑，无需手写
}