package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 套餐业务实现
 */
@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    private final SetmealMapper setmealMapper;

    @Autowired
    public SetmealServiceImpl(SetmealMapper setmealMapper) {
        this.setmealMapper = setmealMapper;
    }

    /**
     * 新增套餐和对应菜品关系
     *
     * @param setmealDTO 套餐数据
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "setmealCache", key = "#setmealDTO.categoryId")
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        if (setmeal.getStatus() == null) {
            setmeal.setStatus(StatusConstant.DISABLE);
        }

        log.info("准备执行SQL：新增套餐主表数据，setmeal={}", setmeal);
        setmealMapper.insert(setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmeal.getId());
            }

            log.info("准备执行SQL：批量新增套餐菜品关系，setmealId={}，setmealDishes={}", setmeal.getId(), setmealDishes);
            setmealMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 批量删除套餐
     *
     * @param ids 套餐id集合
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public void deleteBatch(List<Long> ids) {
        log.info("准备执行SQL：根据套餐id集合查询套餐，ids={}", ids);
        List<Setmeal> setmeals = setmealMapper.listByIds(ids);

        for (Setmeal setmeal : setmeals) {
            if (StatusConstant.ENABLE.equals(setmeal.getStatus())) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        log.info("准备执行SQL：根据套餐id集合批量删除套餐，ids={}", ids);
        setmealMapper.deleteByIds(ids);

        log.info("准备执行SQL：根据套餐id集合批量删除套餐菜品关系，ids={}", ids);
        setmealMapper.deleteDishBySetmealIds(ids);
    }

    /**
     * 修改套餐和对应菜品关系
     *
     * @param setmealDTO 套餐数据
     */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public void updateWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        log.info("准备执行SQL：修改套餐主表数据，setmeal={}", setmeal);
        setmealMapper.update(setmeal);

        Long setmealId = setmealDTO.getId();

        log.info("准备执行SQL：根据套餐id删除原有套餐菜品关系，setmealId={}", setmealId);
        setmealMapper.deleteDishBySetmealId(setmealId);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        if (setmealDishes != null && !setmealDishes.isEmpty()) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmealId);
            }

            log.info("准备执行SQL：重新批量插入套餐菜品关系，setmealId={}，setmealDishes={}", setmealId, setmealDishes);
            setmealMapper.insertBatch(setmealDishes);
        }
    }

    /**
     * 起售、停售套餐
     *
     * @param status 状态
     * @param id 套餐id
     */
    @Override
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public void startOrStop(Integer status, Long id) {
        if (StatusConstant.ENABLE.equals(status)) {
            Integer count = setmealMapper.countDisabledDishBySetmealId(id);
            if (count > 0) {
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();

        log.info("准备执行SQL：修改套餐状态，setmeal={}", setmeal);
        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     *
     * @param setmeal 查询条件
     * @return 套餐列表
     */
    @Override
    @Cacheable(
            cacheNames = "setmealCache",
            key = "#setmeal.categoryId",
            condition = "#setmeal.categoryId != null && #setmeal.status == T(com.sky.constant.StatusConstant).ENABLE && (#setmeal.name == null || #setmeal.name == '')",
            unless = "#result == null || #result.size() == 0"
    )
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }

    /**
     * 根据id查询菜品选项
     *
     * @param id 套餐id
     * @return 套餐内菜品列表
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
