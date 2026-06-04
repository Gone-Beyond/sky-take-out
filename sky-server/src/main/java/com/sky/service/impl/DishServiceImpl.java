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
import com.sky.exception.BaseException;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    @Autowired
    public DishServiceImpl(DishMapper dishMapper, SetmealMapper setmealMapper) {
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
    }

    /**
     * 新增菜品和对应的口味
     *
     * @param dishDTO 菜品数据
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        log.info("准备执行SQL：新增菜品主表数据，dish={}", dish);
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

            log.info("准备执行SQL：批量新增菜品口味数据，dishId={}，flavors={}", dishId, flavors);
            dishMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO 分页查询条件
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {

        PageHelper.startPage(
                dishPageQueryDTO.getPage(),
                dishPageQueryDTO.getPageSize()
        );

        log.info("准备执行SQL：菜品分页查询，dishPageQueryDTO={}", dishPageQueryDTO);
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 删除菜品
     *
     * @param ids 菜品id集合
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {

        // 判断当前菜品是否能够删除，起售中的菜品不能删除
        
        log.info("准备执行SQL：根据菜品id集合查询菜品，ids={}", ids);
        List<Dish> dishes = dishMapper.listByIds(ids);
        for (Dish dish : dishes) {
            if (dish.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 判断当前菜品是否能够删除，被套餐关联的菜品不能删除
        log.info("准备执行SQL：根据菜品id集合查询关联套餐数量，ids={}", ids);
        Integer count = setmealMapper.countByDishIds(ids);
        if (count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        log.info("准备执行SQL：根据菜品id集合批量删除菜品，ids={}", ids);
        dishMapper.deleteByIds(ids);

        log.info("准备执行SQL：根据菜品id集合批量删除菜品口味，ids={}", ids);
        dishMapper.deleteFlavorByDishIds(ids);
    }


    /**
     * 根据菜品 id 查询菜品信息和对应口味
     *
     * @param id 菜品 id
     * @return 菜品详情 VO
     */
    @Override
    public DishVO selectById(Long id) {
        Dish dish = dishMapper.listById(id);

        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);

        List<DishFlavor> flavors = dishMapper.listDishFlavorsByDishId(id);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 修改菜品信息
     *
     * @param dishDTO 菜品数据
     */
    @Transactional
    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        log.info("准备执行SQL：修改菜品主表信息，dish={}", dish);
        dishMapper.updateDish(dish);

        Long dishId = dishDTO.getId();

        log.info("准备执行SQL：根据菜品id删除原有口味数据，dishId={}", dishId);
        dishMapper.deleteFlavorByDishId(dishId);

        List<DishFlavorDTO> dishDTOFlavors = dishDTO.getFlavors();

        if (dishDTOFlavors != null && !dishDTOFlavors.isEmpty()) {
            List<DishFlavor> dishFlavors = new ArrayList<>();

            for (DishFlavorDTO dishDTOFlavor : dishDTOFlavors) {
                DishFlavor dishFlavor = new DishFlavor();
                BeanUtils.copyProperties(dishDTOFlavor, dishFlavor);
                dishFlavor.setDishId(dishId);
                dishFlavors.add(dishFlavor);
            }

            log.info("准备执行SQL：重新批量插入菜品口味数据，dishId={}，dishFlavors={}", dishId, dishFlavors);
            dishMapper.insertBatch(dishFlavors);
        }
    }

}
