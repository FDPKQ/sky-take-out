package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);


    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String outTradeNo);

    void update(Orders orders);

    @Select("select * from orders where number=#{orderNumber}")
    Long getOrderId(String orderNumber);

    @Update("update orders set status = #{orderStatus},pay_status = #{orderPaidStatus} ,checkout_time = #{checkOutTime} where id = #{orderId}")
    void updateStatus(Integer orderStatus, Integer orderPaidStatus, LocalDateTime checkOutTime, Long orderId);

    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime localDateTime);

    void cancelOrderById(List<Long> idList, Integer cancelled, String cancelReason, LocalDateTime time);
}
