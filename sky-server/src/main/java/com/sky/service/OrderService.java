package com.sky.service;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单。
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付，生成微信预支付参数。
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功回调后，修改订单状态。
     */
    void paySuccess(String outTradeNo);

    /**
     * 管理端订单条件分页查询。
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据订单 id 查询订单详情。
     */
    OrderVO details(Long id);

    /**
     * 各状态订单数量统计。
     */
    OrderStatisticsVO statistics();

    /**
     * 接单。
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 拒单。
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 管理端取消订单。
     */
    void cancel(OrdersCancelDTO ordersCancelDTO);

    /**
     * 派送订单。
     */
    void delivery(Long id);

    /**
     * 完成订单。
     */
    void complete(Long id);

    /**
     * User sends an order reminder.
     */
    void reminder(Long id);
}
