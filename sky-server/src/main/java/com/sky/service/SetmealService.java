package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;

import java.util.List;

public interface SetmealService {

    /**
     * 新增套餐和对应菜品关系
     *
     * @param setmealDTO 套餐数据
     */
    void saveWithDish(SetmealDTO setmealDTO);

    /**
     * 批量删除套餐
     *
     * @param ids 套餐id集合
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改套餐和对应菜品关系
     *
     * @param setmealDTO 套餐数据
     */
    void updateWithDish(SetmealDTO setmealDTO);

    /**
     * 起售、停售套餐
     *
     * @param status 状态
     * @param id 套餐id
     */
    void startOrStop(Integer status, Long id);

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
