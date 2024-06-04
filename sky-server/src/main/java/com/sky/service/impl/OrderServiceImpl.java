package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.service.OrderService;
import com.sky.utils.RedisIdWorker;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.alibaba.fastjson.JSONObject;

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

    @Resource
    private UserMapper userMapper;

    @Resource
    private WeChatPayUtil weChatPayUtil;

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

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);


        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal("0.01"), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
//            throw new OrderBusinessException("该订单已支付");
//        }
//
//        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
//        vo.setPackageStr(jsonObject.getString("package"));
//
//        return vo;

        paySuccess(ordersPaymentDTO.getOrderNumber());
        String orderNumber = ordersPaymentDTO.getOrderNumber(); //订单号
        Long orderId = orderMapper.getOrderId(orderNumber);//根据订单号查主键

        JSONObject jsonObject = new JSONObject();//本来没有2
        jsonObject.put("code", "ORDERPAID"); //本来没有3
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        //为替代微信支付成功后的数据库订单状态更新，多定义一个方法进行修改
        Integer OrderPaidStatus = Orders.PAID; //支付状态，已支付
        Integer OrderStatus = Orders.TO_BE_CONFIRMED; //订单状态，待接单

        //发现没有将支付时间 check_out属性赋值，所以在这里更新
        LocalDateTime check_out_time = LocalDateTime.now();
        orderMapper.updateStatus(OrderStatus, OrderPaidStatus, check_out_time, orderId);

        return vo;  //  修改支付方法中的代码

    }

    @Override
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }
}
