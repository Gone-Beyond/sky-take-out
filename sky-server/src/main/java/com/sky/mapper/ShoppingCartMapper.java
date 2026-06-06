package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 动态条件查询购物车数据。
     *
     * 添加购物车时用于判断“当前用户是否已经添加过同一个菜品/套餐”。
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据购物车 id 更新商品数量。
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 新增购物车记录。
     */
    void insert(ShoppingCart shoppingCart);
}
