package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class OrderTask {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Scheduled(cron = "0 * * * * ? ")
    public void processTimeOutOrder() {
        log.info("processTimeOutOrder");
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(
                Orders.PENDING_PAYMENT,
                LocalDateTime.now().plusMinutes(-15));

        if (ordersList == null || ordersList.isEmpty()) {
            return;
        }
        List<Long> idList = ordersList.stream().map(Orders::getId).collect(Collectors.toList());
        orderMapper.cancelOrderById(idList, Orders.CANCELLED, "订单超时", LocalDateTime.now());
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder() {
        log.info("processDeliveryOrder");
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(
                Orders.PENDING_PAYMENT,
                LocalDateTime.now().plusHours(-1));

        if (ordersList == null || ordersList.isEmpty()) {
            return;
        }

        List<Long> idList = ordersList.stream().map(Orders::getId).collect(Collectors.toList());
        orderMapper.cancelOrderById(idList, Orders.COMPLETED, null, null);
    }

    @Scheduled(cron = "0 0/30 * * * ? ")
    public void deleteStatisticsCache() {
        log.info("deleteStatisticsCache");
        Set<String> keys = stringRedisTemplate.keys("statistics*");
        if (keys == null || keys.isEmpty()) {
            return;
        }
        stringRedisTemplate.delete(keys);
    }
}
