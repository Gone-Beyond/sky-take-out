package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.dto.GoodsSalesDTO;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
    private final OrderDetailMapper orderDetailMapper;

    public ReportServiceImpl(OrderMapper orderMapper, UserMapper userMapper, OrderDetailMapper orderDetailMapper) {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.orderDetailMapper = orderDetailMapper;
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

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> salesTop10 = orderDetailMapper.getSalesTop10(beginTime, endTime, Orders.COMPLETED);

        List<String> nameList = new ArrayList<>();
        List<String> numberList = new ArrayList<>();

        for (GoodsSalesDTO goodsSalesDTO : salesTop10) {
            nameList.add(goodsSalesDTO.getName());
            numberList.add(goodsSalesDTO.getNumber().toString());
        }

        return SalesTop10ReportVO.builder()
                .nameList(String.join(",", nameList))
                .numberList(String.join(",", numberList))
                .build();
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate beginDate = endDate.minusDays(29);

        LocalDateTime beginTime = LocalDateTime.of(beginDate, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(endDate, LocalTime.MAX);

        BusinessDataVO businessDataVO = getBusinessData(beginTime, endTime);

        ClassPathResource resource = new ClassPathResource("template/运营数据报表模板.xlsx");

        try (InputStream inputStream = resource.getInputStream();
             XSSFWorkbook excel = new XSSFWorkbook(inputStream)) {

            Sheet sheet = excel.getSheetAt(0);

            getCell(sheet, 1, 1).setCellValue("时间：" + beginDate + "至" + endDate);

            getCell(sheet, 3, 2).setCellValue(businessDataVO.getTurnover());
            getCell(sheet, 3, 4).setCellValue(businessDataVO.getOrderCompletionRate());
            getCell(sheet, 3, 6).setCellValue(businessDataVO.getNewUsers());

            getCell(sheet, 4, 2).setCellValue(businessDataVO.getValidOrderCount());
            getCell(sheet, 4, 4).setCellValue(businessDataVO.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = beginDate.plusDays(i);

                LocalDateTime dayBeginTime = LocalDateTime.of(date, LocalTime.MIN);
                LocalDateTime dayEndTime = LocalDateTime.of(date, LocalTime.MAX);

                BusinessDataVO dailyBusinessData = getBusinessData(dayBeginTime, dayEndTime);

                int rowIndex = 7 + i;

                getCell(sheet, rowIndex, 1).setCellValue(date.toString());
                getCell(sheet, rowIndex, 2).setCellValue(dailyBusinessData.getTurnover());
                getCell(sheet, rowIndex, 3).setCellValue(dailyBusinessData.getValidOrderCount());
                getCell(sheet, rowIndex, 4).setCellValue(dailyBusinessData.getOrderCompletionRate());
                getCell(sheet, rowIndex, 5).setCellValue(dailyBusinessData.getUnitPrice());
                getCell(sheet, rowIndex, 6).setCellValue(dailyBusinessData.getNewUsers());
            }

            String fileName = URLEncoder.encode("运营数据报表.xlsx", StandardCharsets.UTF_8);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

            excel.write(response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException("导出运营数据报表失败", e);
        }
    }

    private BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        Map<String, Object> map = new HashMap<>();
        map.put("begin", begin);
        map.put("end", end);

        Integer totalOrderCount = orderMapper.countByMap(map);

        map.put("status", Orders.COMPLETED);
        Integer validOrderCount = orderMapper.countByMap(map);

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

    private Cell getCell(Sheet sheet, int rowIndex, int cellIndex) {
        if (sheet.getRow(rowIndex) == null) {
            sheet.createRow(rowIndex);
        }

        if (sheet.getRow(rowIndex).getCell(cellIndex) == null) {
            sheet.getRow(rowIndex).createCell(cellIndex);
        }

        return sheet.getRow(rowIndex).getCell(cellIndex);
    }



}
