package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Resource
    private ShoppingCartMapper shoppingCartMapper;
    @Resource
    private DishMapper dishMapper;
    @Resource
    private SetmealMapper setmealMapper;

    /**
     * 添加商品到购物车。
     *
     * @param shoppingCartDTO 购物车DTO对象，包含商品相关信息。
     *                        该方法首先尝试根据传入的购物车DTO对象查找已存在的购物车项。
     *                        如果存在，则数量加1并更新到数据库。
     *                        如果不存在，则根据商品类型（菜品或套餐）获取商品信息，并创建新购物车项添加到数据库。
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 创建一个空购物车对象
        // 商品是否存在
        ShoppingCart shoppingCart = new ShoppingCart();
        // 将DTO对象的属性复制到购物车对象
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        // 设置用户ID
        shoppingCart.setUserId(BaseContext.getCurrentId());
        // 根据购物车信息查询已存在的购物车列表

        // TODO Add Lock
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 如果已存在购物车项，则数量加1并更新
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(cart);
            return;
        }
        // 根据商品类型分别处理菜品和套餐
        Long dishId = shoppingCartDTO.getDishId();
        if (dishId != null) {
            // 查询菜品信息
            List<Dish> dishes = dishMapper.getByIds(Collections.singletonList(dishId));
            // 如果菜品不存在，则直接返回
            if (dishes == null || dishes.isEmpty()) {
                return;
            }
            // 设置购物车项的菜品名称、图片和价格
            shoppingCart.setName(dishes.get(0).getName());
            shoppingCart.setImage(dishes.get(0).getImage());
            shoppingCart.setAmount(dishes.get(0).getPrice());
        } else {
            Long setmealId = shoppingCartDTO.getSetmealId();
            // 查询套餐信息
            List<Setmeal> setmealList = setmealMapper.getByIds(Collections.singletonList(setmealId));
            // 如果套餐不存在，则直接返回
            if (setmealList == null || setmealList.isEmpty()) {
                return;
            }
            // 设置购物车项的套餐名称、图片和价格
            shoppingCart.setName(setmealList.get(0).getName());
            shoppingCart.setImage(setmealList.get(0).getImage());
            shoppingCart.setAmount(setmealList.get(0).getPrice());
        }
        // 设置购物车项的数量为1
        shoppingCart.setNumber(1);
        // 设置购物车项的创建时间
        shoppingCart.setCreateTime(LocalDateTime.now());
        // 新增购物车项到数据库
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * 减少购物车中商品的数量。
     * 该方法首先根据传入的购物车DTO对象创建一个购物车实体对象，并设置用户ID。
     * 然后，根据这个购物车实体对象查询数据库中对应的购物车记录。
     * 如果查询结果存在且不为空，将查询到的购物车商品数量减一。
     * 如果减一后商品数量仍大于0，则更新数据库中该购物车商品的数量。
     * 如果减一后商品数量等于0，则删除该购物车商品的记录。
     *
     * @param shoppingCartDTO 购物车DTO对象，包含购物车的相关信息。
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 创建一个新的ShoppingCart对象，并从DTO中复制属性
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        // 设置用户ID
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 根据购物车信息查询数据库中的购物车列表
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        // 如果查询结果为空，则直接返回
        if (list == null || list.isEmpty()) {
            return;
        }
        // 获取列表中的第一个购物车对象
        ShoppingCart cart = list.get(0);
        // 将购物车商品数量减一
        cart.setNumber(cart.getNumber() - 1);
        // 如果减一后商品数量大于0，则更新数据库中的商品数量
        if (cart.getNumber() > 0) {
            shoppingCartMapper.updateNumberById(cart);
        } else {
            // 如果减一后商品数量等于0，则删除该购物车商品记录
            shoppingCartMapper.deleteByIds(Collections.singletonList(cart.getId()));
        }
    }

    @Override
    public void cleanShoppingCart() {
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(BaseContext.getCurrentId())
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream().map(ShoppingCart::getId).collect(Collectors.toList());
        shoppingCartMapper.deleteByIds(ids);
    }
}
