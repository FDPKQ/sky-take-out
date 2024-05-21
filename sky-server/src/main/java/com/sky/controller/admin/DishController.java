package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {

    @Resource
    private DishService dishService;

    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageResult result = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(result);
    }

    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        dishService.deleteBatch(ids);
        return Result.success();

    }

    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dish = dishService.getById(id);
        return Result.success(dish);
    }

    @PutMapping
    public Result<String> update(@RequestBody DishDTO dishDTO) {

        dishService.updateWithFlavor(dishDTO);
        return Result.success();

    }
}
