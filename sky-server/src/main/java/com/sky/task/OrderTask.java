package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    private final OrderMapper orderMapper;

    public OrderTask(OrderMapper orderMapper) {
        this.orderMapper = orderMapper;
    }


    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder() {
        LocalDateTime time = LocalDateTime.now().minusMinutes(15);

        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);

        log.info("timeout unpaid order count: {}", ordersList.size());

        LocalDateTime now = LocalDateTime.now();

        for (Orders item : ordersList) {
            orderMapper.updateStatusAndCancelInfo(
                    item.getId(),
                    Orders.CANCELLED,
                    "超时未支付，自动取消",
                    now
            );
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {

        List<Orders> ordersList = orderMapper.getByStatus(Orders.DELIVERY_IN_PROGRESS);

        log.info("delivery in progress order count: {}", ordersList.size());

        LocalDateTime now = LocalDateTime.now();
        for (Orders item : ordersList) {
            orderMapper.updateStatusAndDeliveryTime(item.getId(), Orders.COMPLETED, now);
        }
    }
}