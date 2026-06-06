package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

public interface ShoppingCartService {

    /**
     * 添加购物车。
     *
     * Service 层负责处理“已有则数量加一、没有则新增”的业务规则。
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
