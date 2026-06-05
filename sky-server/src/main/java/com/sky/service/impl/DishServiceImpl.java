package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.RedisKeyConstant;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public DishServiceImpl(DishMapper dishMapper, SetmealMapper setmealMapper, RedisTemplate<String, Object> redisTemplate) {
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
        this.redisTemplate = redisTemplate;
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
        cleanDishListCacheByCategoryId(dishDTO.getCategoryId());

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

        dishes.stream()
                .map(Dish :: getCategoryId)
                .distinct()
                .forEach(this ::cleanDishListCacheByCategoryId);
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

        Long oldCategoryId = dishMapper.listById(dishDTO.getId()).getCategoryId();

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
        cleanDishListCacheByCategoryId(oldCategoryId);
        cleanDishListCacheByCategoryId(dishDTO.getCategoryId());
    }

    /**
     * 条件查询菜品和口味（分类ID会缓存）
     *
     * @param dish 查询条件
     * @return 菜品和口味列表
     */
    @Override
    public List<DishVO> listWithFlavor(Dish dish) {

         // 判断当前查询条件是否满足缓存条件：按分类查询、只查启售菜品、且没有按名称搜索
         boolean cacheable = dish.getCategoryId() != null
                 && StatusConstant.ENABLE.equals(dish.getStatus())
                 && (dish.getName() == null || dish.getName().isEmpty());
         log.info("dish list cache check, cacheable={}, categoryId={}, status={}, name={}",
                 cacheable, dish.getCategoryId(), dish.getStatus(), dish.getName());

         String key = null;

         if (cacheable) {
             key = RedisKeyConstant.getDishListCategoryKey(dish.getCategoryId());
             log.info("dish list cache read start, key={}", key);

             Object cacheValue = redisTemplate.opsForValue().get(key);

             

             if (cacheValue != null) {
                 log.info("dish list cache hit, key={}", key);
                 String json = (String) cacheValue;

                 List<DishVO> cachedList = JSON.parseArray(json, DishVO.class);
                 log.info("dish list cache hit parsed, key={}, size={}", key, cachedList == null ? 0 : cachedList.size());
                 return cachedList;
             }
             log.info("dish list cache miss, key={}", key);
         }

        // 根据传入的查询条件查询菜品基础信息
        List<Dish> dishList = dishMapper.list(dish);
        log.info("dish list query db result, categoryId={}, size={}", dish.getCategoryId(), dishList == null ? 0 : dishList.size());

        // 没有查询到菜品时，直接返回空集合，避免后续无效查询
        if (dishList == null || dishList.isEmpty()) {
            return Collections.emptyList();
        }
        // 提取菜品 id 集合，用于批量查询这些菜品对应的口味信息
        List<Long> dishIds = dishList.stream().map(Dish::getId).collect(Collectors.toList());
        // 批量查询所有菜品的口味，避免在循环中逐个查询造成多次数据库访问
        List<DishFlavor> flavorList = dishMapper.listDishFlavorsByDishIds(dishIds);
        // 按菜品 id 对口味列表分组，便于组装每个菜品对应的口味集合
        Map<Long, List<DishFlavor>> flavorMap = flavorList == null
                ? Collections.emptyMap()
                : flavorList.stream().collect(Collectors.groupingBy(DishFlavor::getDishId));
        // 将菜品实体转换为 VO，并为每个菜品设置对应的口味列表
        List<DishVO> dishVOList = dishList.stream().map(
                item -> {
                    DishVO dishVO = new DishVO();

                    // 复制菜品基础属性
                    BeanUtils.copyProperties(item, dishVO);

                    // 如果该菜品没有口味数据，则设置为空集合，避免返回 null
                    dishVO.setFlavors(flavorMap.getOrDefault(item.getId(), Collections.emptyList()));


                    return dishVO;
                }
        ).collect(Collectors.toList());

        if (cacheable) {
            String json = JSON.toJSONString(dishVOList);
            redisTemplate.opsForValue().set(key, json);
            log.info("dish list cache write success, key={}, size={}, jsonLength={}", key, dishVOList.size(), json.length());
        }


        return dishVOList;

    }


    /**
     * 清理某个分类的菜品缓存
     *
     * @param categoryId
     */
    private void cleanDishListCacheByCategoryId(Long categoryId) {
        if (categoryId == null) {
            return;
        }

        String key = RedisKeyConstant.getDishListCategoryKey(categoryId);
        Boolean deleted = redisTemplate.delete(key);
        log.info("dish list cache delete, categoryId={}, key={}, deleted={}", categoryId, key, deleted);

    }

}
