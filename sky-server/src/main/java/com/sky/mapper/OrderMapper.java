package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    @Select("select count(id) from orders where status = #{status}")
    Integer countStatus(Integer status);

    @MapKey("date")
    List<Map<String, Object>> getOrderDaySumFromBeginToEnd(LocalDate begin, LocalDate end);

    Integer countByMap(Map<String, Object> map);


    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin, LocalDateTime end);

    Double sumByMap(Map<String, Object> map);
}
