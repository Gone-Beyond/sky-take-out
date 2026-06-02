package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.dto.DishFlavorDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
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

        if (flavorDTOList != null && !flavorDTOList.isEmpty()) {
            List<DishFlavor> flavors = new ArrayList<>();
            for (DishFlavorDTO dishFlavorDTO : flavorDTOList) {
                DishFlavor flavor = new DishFlavor();
                BeanUtils.copyProperties(dishFlavorDTO, flavor);
                flavor.setDishId(dish.getId());
                flavors.add(flavor);
            }

            dishMapper.insertBatch(flavors);
        }

    }
}
