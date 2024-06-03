package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.annotation.Retention;
import java.util.Collections;
import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Resource
    private SetmealMapper setmealMapper;

    @Resource
    private DishMapper dishMapper;

    @Resource
    private SetmealDishMapper setmealDishMapper;

    @Override
    public void save(SetmealDTO setmealDTO) {

    }

    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        setmealMapper.insert(setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));

        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(), dto.getPageSize());

        Page<SetmealVO> setmealPage = setmealMapper.pageQuery(dto);

        return new PageResult(setmealPage.getTotal(), setmealPage.getResult());
    }


    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        List<Setmeal> setmealList = setmealMapper.getByIds(ids);
        setmealList.forEach(s -> {
            if (s.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE + ": " + s.getName());
            }
        });

        setmealMapper.deleteByIds(ids);
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 根据ID获取套餐信息。
     *
     * @param id 套餐的唯一标识符。
     * @return 返回对应的套餐VO（View Object），如果没有找到对应的套餐，则返回一个空的套餐VO。
     */
    @Override
    public SetmealVO getById(Long id) {
        // 通过ID查询套餐列表，这里使用的是单个ID查询，返回的是一个集合，即便只有一个结果也是以集合形式返回
        List<Setmeal> setmealList = setmealMapper.getByIds(Collections.singletonList(id));

        // 检查查询结果是否为空，若为空则直接返回一个空的SetmealVO对象
        if (setmealList == null || setmealList.isEmpty()) {
            return new SetmealVO();
        }

        // 获取查询到的套餐信息，由于上面的查询结果是一个列表，这里取第一个元素
        Setmeal setmeal = setmealList.get(0);
        // 根据套餐ID查询套餐包含的菜品信息
        List<SetmealDish> setmealDish = setmealDishMapper.getBySetmealId(id);

        // 创建套餐VO对象，并将查询到的套餐信息和菜品信息填充进去
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO); // 复制套餐的基本信息
        setmealVO.setSetmealDishes(setmealDish); // 设置套餐包含的菜品
        return setmealVO;
    }

    /**
     * 更新套餐信息。
     *
     * @param setmealDTO 套餐数据传输对象，包含要更新的套餐及其菜品信息。
     *                   其中，setmealDTO.getId() 用于确定要更新的套餐，
     *                   setmealDTO.getSetmealDishes() 包含该套餐更新后的菜品列表。
     *                   如果setmealDishes为空或列表为空，则不更新菜品信息。
     */
    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        // 创建一个新的Setmeal实例，并从DTO复制属性
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 更新数据库中的套餐信息
        setmealMapper.update(setmeal);

        // 删除该套餐之前的菜品信息，为更新做准备
        setmealDishMapper.deleteBySetmealIds(Collections.singletonList(setmealDTO.getId()));

        // 如果更新后的套餐包含菜品信息，则进行插入操作
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes == null || setmealDishes.isEmpty()) {
            return;
        }
        // 设置菜品所属套餐ID，准备插入
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDTO.getId()));
        // 批量插入更新后的菜品信息
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 条件查询
     *
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }

    /**
     * 根据id查询菜品选项
     *
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 更新套餐的状态。
     *
     * <br>
     * 此方法首先检查套餐状态是否要从禁用状态切换为启用状态。
     * 如果是，将检查该套餐所包含的菜品是否都处于启用状态。
     * 如果有任何菜品处于禁用状态，则抛出异常，防止套餐被错误启用。
     * 最后，更新套餐的状态为提供的新状态。
     *
     * @param id     套餐的唯一标识符，不可为null。
     * @param status 要更新到的新状态，不可为null。
     */
    @Override
    public void updateStatus(Long id, Integer status) {
        // 当套餐状态更新为启用时，进行特别检查
        if (status.equals(StatusConstant.ENABLE)) {
            // 获取套餐对应的菜品列表
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            // 检查菜品列表是否非空且存在禁用状态的菜品
            if (dishList != null && !dishList.isEmpty()) {
                dishList.forEach(dish -> {
                    // 如果发现禁用状态的菜品，则抛出异常
                    if (dish.getStatus().equals(StatusConstant.DISABLE)) {
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED + ": " + dish.getName());
                    }
                });
            }
        }

        // 构建套餐对象，为状态更新做准备
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        // 执行套餐状态的数据库更新操作
        setmealMapper.update(setmeal);
    }


}
