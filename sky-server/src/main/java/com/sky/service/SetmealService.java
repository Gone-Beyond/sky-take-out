package com.sky.service;

import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;

import java.util.List;

public interface SetmealService {

    /**
     * 条件查询
     *
     * @param setmeal 查询条件
     * @return 套餐列表
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     *
     * @param id 套餐id
     * @return 套餐内菜品列表
     */
    List<DishItemVO> getDishItemById(Long id);
}
