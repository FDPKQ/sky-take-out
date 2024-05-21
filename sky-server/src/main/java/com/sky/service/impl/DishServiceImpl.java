package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Resource
    private DishMapper dishMapper;

    @Resource
    private DishFlavorMapper dishFlavorMapper;

    @Resource
    private SetmealDishMapper setmealDishMapper;

    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insert(dish);

        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors == null || flavors.isEmpty()) {
            return;
        }
        flavors.forEach(f -> f.setDishId(dishId));

        dishFlavorMapper.insertBatch(flavors);


    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        List<Dish> dishList = dishMapper.getByIds(ids);
        for (Dish dish : dishList) {
            if (dish.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE + ": " + dish.getName());
            }
        }

        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && !setmealIds.isEmpty()) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL + ": " + setmealIds);
        }

        dishMapper.deleteBatch(ids);
        dishFlavorMapper.deleteByDishIds(ids);
    }

    @Override
    public DishVO getById(Long id) {
        List<Dish> dish = dishMapper.getByIds(Collections.singletonList(id));
        if (dish == null || dish.isEmpty()) {
            return null;
        }
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish.get(0), dishVO);

        List<DishFlavor> dishFlavorList = dishFlavorMapper.getByDishId(dishVO.getId());

        dishVO.setFlavors(dishFlavorList);


        return dishVO;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.update(dish);

        List<DishFlavor> flavors = dishDTO.getFlavors();
        dishFlavorMapper.deleteByDishIds(Collections.singletonList(dishDTO.getId()));
        if (flavors == null || flavors.isEmpty()) {
            return;
        }
        flavors.forEach(f -> f.setDishId(dishDTO.getId()));
        dishFlavorMapper.insertBatch(flavors);
    }
}
