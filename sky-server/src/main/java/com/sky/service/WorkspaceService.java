package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

public interface WorkspaceService {

    /**
     * 查询今日运营数据。
     */
    BusinessDataVO getBusinessData();

    /**
     * 查询订单管理概览。
     */
    OrderOverViewVO getOrderOverView();

    /**
     * 查询菜品总览。
     */
    DishOverViewVO getDishOverView();

    /**
     * 查询套餐总览。
     */
    SetmealOverViewVO getSetmealOverView();
}
