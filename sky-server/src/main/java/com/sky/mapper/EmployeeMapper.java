package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
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
     * 新增员工
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
    @AutoFill(OperationType.INSERT)
    void insert(Employee employee);

    /**
     *  员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 动态更新员工信息
     * <p>
     * 启用或禁用员工账号时，会更新员工状态、最后修改时间和最后修改人。
     *
     * @param employee 员工实体对象，至少需要包含 id 和本次要更新的字段
     * @return 无返回值
     */
    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);


    /**
     * 根据id查询员工
     *
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(Long id);
}
