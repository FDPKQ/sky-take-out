package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
public class SetmealController {
    @Resource
    private SetmealService setmealService;


    @PostMapping
    public Result<String> save(@RequestBody SetmealDTO setmealDTO) {
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO dto) {
        PageResult result = setmealService.pageQuery(dto);
        return Result.success(result);
    }

    @PostMapping("/status/{status}")
    public Result<String> updateStatus(
            @RequestParam Long id,
            @PathVariable Integer status) {
        setmealService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<SetmealVO> getById(@PathVariable Long id) {
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result<String> update(@RequestBody SetmealDTO setmealDTO) {
        setmealService.update(setmealDTO);
        return Result.success();
    }
}
