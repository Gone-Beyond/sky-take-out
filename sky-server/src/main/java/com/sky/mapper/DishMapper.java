package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishDTO;
import com.sky.dto.DishFlavorDTO;
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

    /**
     * 根据菜品id查询菜品信息
     *
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish listById(Long id);

    /**
     * 根据菜品 id 查询该菜品关联的所有口味信息
     *
     * @param dishId 菜品 id
     * @return 菜品口味集合
     */
    @Select("select * from dish_flavor where dish_id = #{dishId}")
    List<DishFlavor> listDishFlavorsByDishId(Long dishId);

    /**
     * 修改菜品信息
     *
     * @param dish
     */
    @AutoFill(OperationType.UPDATE)
    void updateDish(Dish dish);

    /**
     * 动态条件查询菜品
     *
     * @param dish 查询条件
     * @return 菜品列表
     */
    List<Dish> list(Dish dish);

    /**
     * 根据菜品 id 删除该菜品关联的所有口味数据
     *
     * @param dishId 菜品 id
     */
    @Delete("delete from dish_flavor where dish_id = #{dishId}")
    void deleteFlavorByDishId(Long dishId);
}
