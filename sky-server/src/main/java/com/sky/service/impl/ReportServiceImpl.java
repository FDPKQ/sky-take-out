package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //存放从begin到end之间的每天对应的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Map<String, LocalDateTime> map = new HashMap<>();
            map.put("end", endTime);

            //总用户数量
            Integer totalUser = userMapper.countByMap(map);

            map.put("begin", beginTime);
            //新增用户数量
            Integer newUser = userMapper.countByMap(map);

            totalUserList.add(totalUser);
            newUserList.add(newUser);
        }

        //封装结果数据
        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
    }
}
