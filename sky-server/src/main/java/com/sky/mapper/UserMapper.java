package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据 openid 获取用户。
     */
    @Select("select * from user where openid = #{openid}")
    User getByOpenid(String openid);

    /**
     * 根据用户 id 查询用户。
     */
    @Select("select * from user where id = #{id}")
    User getById(Long id);

    /**
     * 插入用户。
     */
    @Insert("insert into user (openid, name, phone, sex, id_number, avatar, create_time) " +
            "values (#{openid}, #{name}, #{phone}, #{sex}, #{idNumber}, #{avatar}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);

    /**
     * 统计指定时间段内新增用户数量。
     */
    @Select("select count(id) from user where create_time >= #{begin} and create_time <= #{end}")
    Integer countByCreateTime(@Param("begin") LocalDateTime begin, @Param("end") LocalDateTime end);

    Long countByMap(Map<String, Object> map);

    Long countTotalByMap(Map<String, Object> map);
}
