package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入菜品主表
     *
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into dish (name, category_id, price, image, description, status, create_time, update_time, create_user, update_user) " +
            "values (#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Dish dish);


    /**
     * 批量插入口味表
     *
     * @param flavors
     */
    void insertBatch(@Param("flavors") List<DishFlavor> flavors);

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据菜品id集合查询菜品
     *
     * @param ids 菜品id集合
     * @return 菜品集合
     */
    List<Dish> listByIds(@Param("ids") List<Long> ids);

    /**
     * 根据菜品id集合批量删除菜品
     *
     * @param ids 菜品id集合
     */
    void deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据菜品id集合批量删除菜品口味
     *
     * @param ids 菜品id集合
     */
    void deleteFlavorByDishIds(@Param("ids") List<Long> ids);
}
