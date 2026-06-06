package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartMapper shoppingCartMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    public ShoppingCartServiceImpl(ShoppingCartMapper shoppingCartMapper, DishMapper dishMapper, SetmealMapper setmealMapper) {
        this.shoppingCartMapper = shoppingCartMapper;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
    }

    /**
     * 添加购物车。
     *
     * 核心规则：
     * 1. 同一个用户、同一个菜品、同一个口味，购物车已有则数量加一；
     * 2. 同一个用户、同一个套餐，购物车已有则数量加一；
     * 3. 购物车没有该商品时，后端补齐商品名称、图片、价格后插入新记录。
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("开始处理添加购物车，userId={}，参数={}", userId, shoppingCartDTO);

        // 前端只传关键 id 和口味，先转换成购物车实体作为查重条件。
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);

        // 菜品按 dishId + dishFlavor 查重；套餐按 setmealId 查重。
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
            ShoppingCart item = shoppingCartList.get(0);
            item.setNumber(item.getNumber() + 1);
            shoppingCartMapper.updateNumberById(item);
            log.info("购物车已有商品，数量加一，cartId={}，number={}", item.getId(), item.getNumber());
            return;
        }

        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        if (dishId != null) {
            // 添加菜品时，后端从 dish 表补齐购物车展示所需的冗余字段。
            Dish dish = dishMapper.listById(dishId);
            if (dish == null) {
                throw new ShoppingCartBusinessException("菜品不存在");
            }
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
            log.info("添加菜品到购物车，dishId={}，name={}", dishId, dish.getName());
        } else {
            // 添加套餐时，后端从 setmeal 表补齐购物车展示所需的冗余字段。
            Setmeal setmeal = setmealMapper.getById(setmealId);
            if (setmeal == null) {
                throw new ShoppingCartBusinessException("套餐不存在");
            }
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
            log.info("添加套餐到购物车，setmealId={}，name={}", setmealId, setmeal.getName());
        }

        // 第一次加入购物车时数量固定为 1。
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
        log.info("新增购物车记录成功，userId={}，dishId={}，setmealId={}", userId, dishId, setmealId);
    }

    /**
     * 查看当前登录用户的购物车列表。
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        log.info("开始查看购物车，userId={}", userId);

        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        log.info("查看购物车完成，userId={}，数量={}", userId, list.size());
        return list;
    }

    /**
     * 清空当前登录用户的购物车。
     */
    @Override
    public void cleanShoppingCart() {
        Long userId = BaseContext.getCurrentId();
        log.info("开始清空购物车，userId={}", userId);

        shoppingCartMapper.deleteByUserId(userId);

        log.info("清空购物车完成，userId={}", userId);
    }
}
