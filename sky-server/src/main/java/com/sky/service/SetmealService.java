package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    void save(SetmealDTO setmealDTO);

    void saveWithDish(SetmealDTO setmealDTO);

    PageResult pageQuery(SetmealPageQueryDTO dto);

    void updateStatus(Long id, Integer status);

    void deleteBatch(List<Long> ids);

    SetmealVO getById(Long id);

    void update(SetmealDTO setmealDTO);

    List<Setmeal> listById(Long categoryId);

    List<DishItemVO> getDishItemById(Long id);
}
