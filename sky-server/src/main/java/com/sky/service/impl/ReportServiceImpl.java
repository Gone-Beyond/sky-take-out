package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;

    public ReportServiceImpl(OrderMapper orderMapper, UserMapper userMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> turnoverList = new ArrayList<>();

        LocalDate currDate = begin;

        while (!currDate.isAfter(end)) {
            dateList.add(currDate.toString());

            LocalDateTime beginTime = LocalDateTime.of(currDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(currDate, LocalTime.MAX);

            BigDecimal turnover = orderMapper.sumAmountByTimeAndStatus(beginTime, endTime, Orders.COMPLETED);

            turnoverList.add(turnover == null ? "0" : turnover.toString());

            currDate = currDate.plusDays(1);
        }

        return TurnoverReportVO.builder()
                .dateList(String.join(",", dateList))
                .turnoverList(String.join(",", turnoverList))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<String> dateList = new ArrayList<>();
        List<String> newUserList = new ArrayList<>();
        List<String> totalUserList = new ArrayList<>();

        Map<String, Object> map = new HashMap<>();
        LocalDate currDate = begin;

        while (!currDate.isAfter(end)) {
            dateList.add(currDate.toString());

            LocalDateTime beginTime = LocalDateTime.of(currDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(currDate, LocalTime.MAX);

            map.clear();
            map.put("begin", beginTime);
            map.put("end", endTime);
            Long newUserCount = userMapper.countByMap(map);

            map.clear();
            map.put("end", endTime);
            Long totalUserCount = userMapper.countTotalByMap(map);

            newUserList.add(newUserCount.toString());
            totalUserList.add(totalUserCount.toString());

            currDate = currDate.plusDays(1);
        }

        return UserReportVO.builder()
                .dateList(String.join(",", dateList))
                .totalUserList(String.join(",", totalUserList))
                .newUserList(String.join(",", newUserList))
                .build();
    }


//
//    //订单总数
//    private Integer totalOrderCount;
//
//    //有效订单数
//    private Integer validOrderCount;
//
//    //订单完成率
//    private Double orderCompletionRate;


    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {

        Map<String, Object> map = new HashMap<>();

        map.put("begin", LocalDateTime.of(begin, LocalTime.MIN));
        map.put("end", LocalDateTime.of(end, LocalTime.MAX));

        Integer totalOrderCount = orderMapper.countByMap(map);

        map.put("status", Orders.COMPLETED);

        Integer validOrderCount = orderMapper.countByMap(map);

        Double orderCompletionRate = 0.0;

        if (totalOrderCount != null && totalOrderCount != 0 && validOrderCount != null) {
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
        }

        List<String> dateList = new ArrayList<>();
        List<String> orderCountList = new ArrayList<>();
        List<String> validOrderCountList = new ArrayList<>();

        LocalDate currDate = begin;

        while (!currDate.isAfter(end)) {
            dateList.add(currDate.toString());

            LocalDateTime beginTime = LocalDateTime.of(currDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(currDate, LocalTime.MAX);

            map.clear();
            map.put("begin", beginTime);
            map.put("end", endTime);

            Integer todayTotalOrderCount = orderMapper.countByMap(map);


            map.put("status", Orders.COMPLETED);
            Integer todayValidOrderCount = orderMapper.countByMap(map);

            orderCountList.add(todayTotalOrderCount.toString());
            validOrderCountList.add(todayValidOrderCount.toString());

            currDate = currDate.plusDays(1);
        }


        return OrderReportVO.builder()
                .dateList(String.join(",", dateList))
                .orderCountList(String.join(",", orderCountList))
                .validOrderCountList(String.join(",", validOrderCountList))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


}
