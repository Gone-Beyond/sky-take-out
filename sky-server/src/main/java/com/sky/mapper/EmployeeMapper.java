package com.sky.mapper;

import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     *新增员工
     *
     * @param employee
     * 员工实体对象，包含用户名、姓名、手机号、性别、身份证号、状态、创建时间、修改时间、创建人、修改人等信息
     */
    @Insert("insert into employee (" +
            "name, " +
            "username, " +
            "password, " +
            "phone, " +
            "sex, " +
            "id_number, " +
            "status, " +
            "create_time, " +
            "update_time, " +
            "create_user, " +
            "update_user" +
            ") values (" +
            "#{name}, " +
            "#{username}, " +
            "#{password}, " +
            "#{phone}, " +
            "#{sex}, " +
            "#{idNumber}, " +
            "#{status}, " +
            "#{createTime}, " +
            "#{updateTime}, " +
            "#{createUser}, " +
            "#{updateUser}" +
            ")")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Employee employee);
}
