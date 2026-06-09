package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单主表，下单后需要拿到数据库生成的订单 id。
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Orders orders);

    /**
     * 根据订单号查询订单。
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 动态修改订单信息。
     */
    void update(Orders orders);

    /**
     * 管理端订单条件分页查询。
     */
    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据 id 查询订单。
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据状态统计订单数量。
     */
    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 统计指定时间段内指定状态的订单数量。
     */
    Integer countByMap(Map<String, Object> map);

    /**
     * 统计指定时间段内指定状态订单的实收金额。
     */
    BigDecimal sumAmountByTimeAndStatus(@Param("begin") LocalDateTime begin,
                                        @Param("end") LocalDateTime end,
                                        @Param("status") Integer status);

    /**
     * 统计指定时间段内指定状态订单的实收金额。
     */


    /**
     * 根据订单状态和下单截止时间，查询订单列表
     *
     *
     * @param status
     * @param orderTime
     * @return List<Orders>
     */
    List<Orders> getByStatusAndOrderTimeLT(@Param("status") Integer status,
                                           @Param("orderTime") LocalDateTime orderTime);

    void updateStatusAndCancelInfo(@Param("id") Long id,
                                   @Param("status") Integer status,
                                   @Param("cancelReason") String cancelReason,
                                   @Param("cancelTime") LocalDateTime cancelTime);

    List<Orders> getByStatus(Integer status);

    void updateStatusAndDeliveryTime(@Param("id") Long id,
                                     @Param("status") Integer status,
                                     @Param("deliveryTime") LocalDateTime deliveryTime);
}
