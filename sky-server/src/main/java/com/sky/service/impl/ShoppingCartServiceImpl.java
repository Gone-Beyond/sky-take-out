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

        // 用户端接口需要从 JWT 拦截器写入的 ThreadLocal 中取得当前登录用户 id。
        Long userId = BaseContext.getCurrentId();
        log.info("开始处理添加购物车，userId={}，参数={}", userId, shoppingCartDTO);

        // 先把前端传入的 DTO 转成实体查询条件，再补上当前用户 id。
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);

        // 查询当前用户购物车中是否已经存在同一菜品/套餐。
        // 菜品会按 dishId + dishFlavor 区分，套餐按 setmealId 区分。
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
            // 添加菜品：前端只传 dishId，购物车表展示需要的名称、图片、价格由后端补齐。
            Dish dish = dishMapper.listById(dishId);
            if (dish == null) {
                throw new ShoppingCartBusinessException("菜品不存在");
            }
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
            log.info("添加菜品到购物车，dishId={}，name={}", dishId, dish.getName());
        } else {
            // 添加套餐：前端只传 setmealId，同样由后端补齐购物车冗余字段。
            Setmeal setmeal = setmealMapper.getById(setmealId);
            if (setmeal == null) {
                throw new ShoppingCartBusinessException("套餐不存在");
            }
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
            log.info("添加套餐到购物车，setmealId={}，name={}", setmealId, setmeal.getName());
        }

        // 新商品第一次加入购物车，数量固定为 1，并记录创建时间。
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
        log.info("新增购物车记录成功，userId={}，dishId={}，setmealId={}", userId, dishId, setmealId);
    }
}
