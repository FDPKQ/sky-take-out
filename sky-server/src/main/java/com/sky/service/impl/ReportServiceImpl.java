package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private UserMapper userMapper;


    /**
     * 根据指定的开始和结束日期，获取这段时间内的营业额统计报告。
     * 如果开始日期晚于结束日期，返回null。
     *
     * @param begin 统计的开始日期
     * @param end   统计的结束日期
     * @return 营业额统计报告对象，包含日期列表和营业额列表。
     * 如果开始日期晚于结束日期，返回null。
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 检查开始日期是否晚于结束日期，如果是，则直接返回null
        if (begin.isAfter(end)) {
            return null;
        }
        // 通过订单Mapper查询从开始日期到结束日期每天的订单总额
        List<Map<String, Object>> result = orderMapper.getOrderDaySumFromBeginToEnd(begin, end);
        // 将查询结果转换为 LocalDate 对应的营业额映射
        Map<LocalDate, Double> turnoverMap = result.stream()
                .collect(Collectors.toMap(
                        r -> ((java.sql.Date) r.get("date")).toLocalDate(),
                        r -> ((Number) r.get("daily_turnover")).doubleValue()
                ));

        // 初始化日期列表和营业额列表
        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();

        // 遍历整个日期范围，确保每一天都有营业额数据
        LocalDate date = begin;
        while (!date.isAfter(end)) {
            // 获取当天的营业额，如果不存在则默认为0.0
            turnoverList.add(turnoverMap.getOrDefault(date, 0.0));
            dateList.add(date);
            date = date.plusDays(1);
        }

        // 构建并返回营业额统计报告对象
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }


    /**
     * 获取用户统计信息。
     * 根据指定的开始和结束日期，统计每天的新增用户数和总用户数。
     *
     * @param begin 统计的开始日期
     * @param end   统计的结束日期
     * @return UserReportVO 对象，包含每天的日期、新增用户数和总用户数
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 初始化日期列表，用于存储从开始日期到结束日期之间的每一天
        List<LocalDate> dateList = getLocalDates(begin, end);

        // 初始化新增用户数和总用户数的列表
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        // 遍历日期列表，统计每天的新增用户数和总用户数
        for (LocalDate date : dateList) {
            // 构造每天的开始和结束时间，用于精确统计用户数
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            // 使用HashMap存储开始和结束时间，用于查询用户数
            Map<String, LocalDateTime> map = new HashMap<>();
            map.put("end", endTime);

            // 查询并获取当天的总用户数
            //总用户数量
            Integer totalUser = userMapper.countByMap(map);

            // 更新HashMap中的开始时间，查询并获取当天的新增用户数
            map.put("begin", beginTime);
            //新增用户数量
            Integer newUser = userMapper.countByMap(map);

            // 将每天的新增用户数和总用户数添加到对应的列表中
            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        // 使用StringUtils的join方法将日期列表、新增用户数列表和总用户数列表转换为字符串，方便在VO中存储和传输
        // 封装统计结果到UserReportVO对象中并返回
        //封装结果数据
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }

    /**
     * 获取订单统计信息。
     * 该方法用于根据指定的开始和结束日期，统计每个日期内的订单总数以及完成订单的数量。
     *
     * @param begin 统计的开始日期
     * @param end   统计的结束日期
     * @return 返回包含订单统计信息的OrderReportVO对象
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        // 初始化日期列表，用于存储从开始日期到结束日期的所有日期
        List<LocalDate> dateList = getLocalDates(begin, end);

        // 初始化订单总数列表和完成订单总数列表
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        // 遍历日期列表，对每个日期统计订单总数和完成订单总数
        dateList.forEach(localDate -> {
            // 创建订单统计查询的参数映射
            Map<String, Object> map = new HashMap<>();
            map.put("begin", LocalDateTime.of(localDate, LocalTime.MIN));
            map.put("end", LocalDateTime.of(localDate, LocalTime.MAX));
            map.put("status", null); // 不限定订单状态，查询所有订单数量
            Integer orderCount = orderMapper.countByMap(map);
            orderCountList.add(orderCount);

            // 修改参数映射中的订单状态为已完成，查询完成订单数量
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByMap(map);
            validOrderCountList.add(validOrderCount);

        });

        // 计算所有完成订单的总数和所有订单的总数
        Integer validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        int totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();

        // 使用OrderReportVO构建器创建并返回订单统计信息对象
        return OrderReportVO.builder()
                .validOrderCount(validOrderCount)
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .dateList(StringUtils.join(dateList, ","))
                .totalOrderCount(totalOrderCount)
                .orderCountList(StringUtils.join(orderCountList, ","))
                .orderCompletionRate(totalOrderCount == 0 ? 0 : validOrderCount.doubleValue() / (double) totalOrderCount)
                .build();
    }

    private List<LocalDate> getLocalDates(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        // 通过逐日增加方式填充日期列表，直到达到结束日期
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
}
