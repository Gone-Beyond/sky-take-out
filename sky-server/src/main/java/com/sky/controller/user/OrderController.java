package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端订单相关接口。
 */
@RequestMapping("/user/order")
@RestController
@Slf4j
@Api(tags = "用户端订单相关接口")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 用户提交订单(下单)
     */
    @ApiOperation("用户提交订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户端提交订单，参数：{}", ordersSubmitDTO);

        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);

        log.info("用户端提交订单成功，结果：{}", orderSubmitVO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付，调用微信支付生成预支付交易单。
     */
    @ApiOperation("订单支付")
    @PutMapping("/payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("用户端订单支付，参数：{}", ordersPaymentDTO);

        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);

        log.info("生成微信预支付交易单成功，结果：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }
    /**
     * User sends an order reminder.
     */
    @ApiOperation("用户催单")
    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id) {
        log.info("user reminder order, id: {}", id);

        orderService.reminder(id);

        return Result.success();
    }
}
