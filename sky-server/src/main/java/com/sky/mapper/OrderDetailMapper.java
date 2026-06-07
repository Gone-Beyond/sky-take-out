package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细。
     *
     * 每条购物车记录会转换成一条订单明细记录。
     */
    void insertBatch(@Param("orderDetailList") List<OrderDetail> orderDetailList);
}
