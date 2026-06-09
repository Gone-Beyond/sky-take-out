package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细。
     */
    void insertBatch(@Param("orderDetailList") List<OrderDetail> orderDetailList);

    /**
     * 根据订单 id 查询订单明细。
     */
    @Select("select * from order_detail where order_id = #{orderId}")
    List<OrderDetail> listByOrderId(Long orderId);
}
