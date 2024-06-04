package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.utils.RedisIdWorker;
import com.sky.vo.OrderSubmitVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    public static final BigDecimal FREIGHT = BigDecimal.valueOf(6);
    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderDetailMapper orderDetailMapper;

    @Resource
    private ShoppingCartMapper shoppingCartMapper;

    @Resource
    private AddressBookMapper addressBookMapper;

    @Resource
    private RedisIdWorker redisIdWorker;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // check
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // list shopping cart
        List<ShoppingCart> list = shoppingCartMapper.list(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());

        if (list == null || list.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // check price
        processCartList(list, ordersSubmitDTO);

        // order + 1
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(redisIdWorker.nextId("order")));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());

        orderMapper.insert(orders);
        // order detail + n
        List<OrderDetail> orderDetailList = list.stream().map(shoppingCart -> {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            return orderDetail;
        }).collect(Collectors.toList());
        orderDetailMapper.insertBatch(orderDetailList);

        // clear shopping cart
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
    }

    public void processCartList(List<ShoppingCart> list, OrdersSubmitDTO ordersSubmitDTO) {
        // 初始化总价为0，避免了之前在循环中不必要地累加BigDecimal.ONE
        BigDecimal price = BigDecimal.ZERO;


        for (ShoppingCart cart : list) {
            // 检查getAmount()和getNumber()返回值是否为null
            BigDecimal amount = cart.getAmount() != null ? cart.getAmount() : BigDecimal.ZERO;
            BigDecimal number = cart.getNumber() != null ? BigDecimal.valueOf(cart.getNumber()) : BigDecimal.ZERO;
            price = price.add(amount.multiply(number));
        }

        // 额外加上的固定费用，
        price = price.add(FREIGHT);
        price = price.add(BigDecimal.valueOf(list.stream().mapToInt(ShoppingCart::getNumber).sum()));

        if (price.compareTo(ordersSubmitDTO.getAmount()) == 0) {
            log.info("Amount is right");
        } else {
            log.error("Amount is not right, expect: {} get: {}", price, ordersSubmitDTO.getAmount());
            throw new OrderBusinessException(MessageConstant.PRICE_ERROR);
        }
    }
}
