package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;

    public WorkspaceServiceImpl(OrderMapper orderMapper,
                                UserMapper userMapper,
                                DishMapper dishMapper,
                                SetmealMapper setmealMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
    }

    @Override
    public BusinessDataVO getBusinessData() {
        LocalDateTime begin = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime end = LocalDateTime.now();

        Map<String, Object> countMap = new HashMap<>();
        countMap.put("begin", begin);
        countMap.put("end", end);

        Integer totalOrderCount = orderMapper.countByMap(countMap);

        countMap.put("status", Orders.COMPLETED);
        Integer validOrderCount = orderMapper.countByMap(countMap);

        BigDecimal turnoverAmount = orderMapper.sumAmountByTimeAndStatus(begin, end, Orders.COMPLETED);
        Integer newUsers = userMapper.countByCreateTime(begin, end);

        double turnover = turnoverAmount == null ? 0.0 : turnoverAmount.doubleValue();
        double orderCompletionRate = calculateRate(validOrderCount, totalOrderCount);
        double unitPrice = calculateUnitPrice(turnoverAmount, validOrderCount);

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }

    @Override
    public OrderOverViewVO getOrderOverView() {
        Integer waitingOrders = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer deliveredOrders = orderMapper.countStatus(Orders.CONFIRMED);
        Integer completedOrders = orderMapper.countStatus(Orders.COMPLETED);
        Integer cancelledOrders = orderMapper.countStatus(Orders.CANCELLED);
        Integer allOrders = orderMapper.countByMap(new HashMap<>());

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    @Override
    public DishOverViewVO getDishOverView() {
        return DishOverViewVO.builder()
                .sold(dishMapper.countByStatus(StatusConstant.ENABLE))
                .discontinued(dishMapper.countByStatus(StatusConstant.DISABLE))
                .build();
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {
        return SetmealOverViewVO.builder()
                .sold(setmealMapper.countByStatus(StatusConstant.ENABLE))
                .discontinued(setmealMapper.countByStatus(StatusConstant.DISABLE))
                .build();
    }

    private double calculateRate(Integer numerator, Integer denominator) {
        if (numerator == null || denominator == null || denominator == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private double calculateUnitPrice(BigDecimal turnover, Integer validOrderCount) {
        if (turnover == null || validOrderCount == null || validOrderCount == 0) {
            return 0.0;
        }
        return turnover
                .divide(BigDecimal.valueOf(validOrderCount), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
