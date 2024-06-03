package com.sky.controller.admin;

import cn.hutool.core.text.StrBuilder;
import com.sky.constant.RedisConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/admin/dish")
public class DishController {

    @Resource
    private DishService dishService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping
    public Result<String> save(@RequestBody DishDTO dishDTO) {
        dishService.saveWithFlavor(dishDTO);
        stringRedisTemplate.delete(RedisConstant.DISH_KEY + dishDTO);
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

        deleteRedisAllDishKeys();

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
        deleteRedisAllDishKeys();
        return Result.success();
    }

    /**
     * 删除Redis中所有与菜品相关的键。
     * <p>
     * 本方法通过查询以DISH_KEY前缀开头的所有键，然后批量删除这些键。
     * 这样做的目的是为了在系统需要清理缓存或重新加载菜品数据时，能够快速地清除与菜品相关的所有缓存数据。
     * 使用Redis的keys方法和delete方法，可以有效地批量处理键，提高操作效率。
     * <p>
     * 注意：批量删除操作应谨慎使用，确保不会误删其他重要数据。
     */
    private void deleteRedisAllDishKeys() {
        // 查询以DISH_KEY前缀开头的所有键
        Set<String> keys = stringRedisTemplate.keys(RedisConstant.DISH_KEY + "*");
        // 检查查询到的键是否为空，非空则删除
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }


    /**
     * 根据分类id查询菜品
     *
     * @param categoryId id
     * @return success
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId) {
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }


    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        dishService.startOrStop(status, id);
        deleteRedisAllDishKeys();
        return Result.success();
    }
}
