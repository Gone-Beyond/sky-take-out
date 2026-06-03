package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishFlavorDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        dishMapper.insert(dish);

        List<DishFlavorDTO> flavorDTOList = dishDTO.getFlavors();

        Long dishId = dish.getId();

        if (flavorDTOList != null && !flavorDTOList.isEmpty()) {
            List<DishFlavor> flavors = new ArrayList<>();
            for (DishFlavorDTO dishFlavorDTO : flavorDTOList) {
                DishFlavor flavor = new DishFlavor();
                BeanUtils.copyProperties(dishFlavorDTO, flavor);
                flavor.setDishId(dishId);
                flavors.add(flavor);
            }

            dishMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(
                dishPageQueryDTO.getPage(),
                dishPageQueryDTO.getPageSize()
        );

        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);


        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 删除菜品
     *
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {

        // 判断当前菜品是否能够删除，起售中的菜品不能删除
        List<Dish> dishes = dishMapper.listByIds(ids);
        for (Dish dish : dishes) {
            if (dish.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 判断当前菜品是否能够删除，被套餐关联的菜品不能删除
        Integer count = setmealMapper.countByDishIds(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品表中的菜品数据
        dishMapper.deleteByIds(ids);

        // 删除菜品关联的口味数据
        dishMapper.deleteFlavorByDishIds(ids);
    }


}
