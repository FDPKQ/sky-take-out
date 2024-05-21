package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.impl.CategoryServiceImpl;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
@Api("分类相关接口")
public class CategoryController {
    @Resource
    private CategoryService categoryService;


    @PutMapping
    public Result<String> update(@RequestBody CategoryDTO categoryDTO) {
        categoryService.update(categoryDTO);
        return Result.success();
    }


    @GetMapping("/page")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status,
                                       @RequestParam Long id) {
        categoryService.updateStatus(status, id);
        return Result.success();
    }

    @PostMapping
    public Result<String> add(@RequestBody CategoryDTO categoryDTO) {
        categoryService.save(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    public Result<String> deleteById(@RequestParam Long id) {
        categoryService.deleteById(id);
        return Result.success();

    }

    @GetMapping("/list")
    public Result<List<Category>> getByType(Integer type) {
        List<Category> list = categoryService.getByType(type);
        return Result.success(list);
    }


}
