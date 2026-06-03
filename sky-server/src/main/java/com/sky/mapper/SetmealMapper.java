package com.sky.mapper;

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
}
