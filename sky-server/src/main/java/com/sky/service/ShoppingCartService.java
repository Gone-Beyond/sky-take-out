package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {

    /**
     * 添加购物车。
     *
     * 已有同款商品时数量加一，没有时新增购物车记录。
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查看当前登录用户的购物车列表。
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空当前登录用户的购物车。
     */
    void cleanShoppingCart();
}
