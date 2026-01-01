package com.example.labequipment.controller;

import com.example.labequipment.common.result.Result;
import com.example.labequipment.entity.Category;
import com.example.labequipment.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category") // ✅ 保持 /api 前缀，复用你的 request.js 配置
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    // 1. 列表查询
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    // 2. 新增分类
    @PostMapping("/add")
    public Result<String> add(@RequestBody Category category) {
        // 可以加个简单的查重逻辑（可选）
        categoryService.save(category);
        return Result.success("新增成功");
    }

    // 3. 修改分类 (本次补充)
    @PutMapping("/update")
    public Result<String> update(@RequestBody Category category) {
        boolean success = categoryService.updateById(category);
        return success ? Result.success("修改成功") : Result.error(500, "修改失败");
    }

    // 4. 删除分类 (本次补充)
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        // 真实业务中，通常要检查该分类下是否有设备，防止误删
        // 这里为了简单，直接删除
        boolean success = categoryService.removeById(id);
        return success ? Result.success("删除成功") : Result.error(500, "删除失败");
    }
}