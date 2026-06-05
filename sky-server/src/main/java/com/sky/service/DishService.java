package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 删除菜品
     *
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据id查询菜品
     *
     * @param id
     * @return
     */
    DishVO selectById(Long id);

    /**
     * 修改菜品信息
     *
     * @param dishDTO
     */
    void update(DishDTO dishDTO);

    /**
     * 条件查询菜品和口味
     *
     * @param dish 查询条件
     * @return 菜品和口味列表
     */
    List<DishVO> listWithFlavor(Dish dish);
}
