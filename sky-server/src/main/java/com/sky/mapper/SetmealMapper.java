package com.sky.mapper;

import com.sky.entity.Setmeal;
import com.sky.vo.DishItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param categoryId
     * @return
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
     * 动态条件查询套餐
     *
     * @param setmeal 查询条件
     * @return 套餐列表
     */
    List<Setmeal> list(Setmeal setmeal);

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
