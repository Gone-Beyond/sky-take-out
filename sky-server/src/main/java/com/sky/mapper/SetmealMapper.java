package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐数量
     *
     * @param categoryId 分类id
     * @return 套餐数量
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 根据菜品id集合查询关联套餐数量
     *
     * @param ids 菜品id集合
     * @return 关联套餐数量
     */
    @Select("<script>" +
            "select count(id) from setmeal_dish where dish_id in " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    Integer countByDishIds(@Param("ids") List<Long> ids);

    /**
     * 新增套餐
     *
     * @param setmeal 套餐
     */
    @AutoFill(OperationType.INSERT)
    @Insert("insert into setmeal (category_id, name, price, status, description, image, create_time, update_time, create_user, update_user) " +
            "values (#{categoryId}, #{name}, #{price}, #{status}, #{description}, #{image}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Setmeal setmeal);

    /**
     * 批量新增套餐菜品关系
     *
     * @param setmealDishes 套餐菜品关系集合
     */
    void insertBatch(@Param("setmealDishes") List<SetmealDish> setmealDishes);

    /**
     * 动态条件查询套餐
     *
     * @param setmeal 查询条件
     * @return 套餐列表
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id集合查询套餐
     *
     * @param ids 套餐id集合
     * @return 套餐列表
     */
    List<Setmeal> listByIds(@Param("ids") List<Long> ids);

    /**
     * 根据id查询套餐
     *
     * @param id 套餐id
     * @return 套餐
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    /**
     * 修改套餐
     *
     * @param setmeal 套餐
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 根据id集合删除套餐
     *
     * @param ids 套餐id集合
     */
    void deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 根据套餐id删除套餐菜品关系
     *
     * @param setmealId 套餐id
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{setmealId}")
    void deleteDishBySetmealId(Long setmealId);

    /**
     * 根据套餐id集合删除套餐菜品关系
     *
     * @param ids 套餐id集合
     */
    void deleteDishBySetmealIds(@Param("ids") List<Long> ids);

    /**
     * 根据套餐id查询套餐内停售菜品数量
     *
     * @param setmealId 套餐id
     * @return 停售菜品数量
     */
    @Select("select count(*) " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId} and d.status = 0")
    Integer countDisabledDishBySetmealId(Long setmealId);

    /**
     * 根据套餐id查询菜品选项
     *
     * @param setmealId 套餐id
     * @return 套餐内菜品列表
     */
    @Select("select sd.name, sd.copies, d.image, d.description " +
            "from setmeal_dish sd left join dish d on sd.dish_id = d.id " +
            "where sd.setmeal_id = #{setmealId}")
    List<DishItemVO> getDishItemBySetmealId(Long setmealId);
}
