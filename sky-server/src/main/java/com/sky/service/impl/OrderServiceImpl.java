package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.entity.User;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final BigDecimal PACK_AMOUNT_UNIT = BigDecimal.ONE;
    private static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(6);
    private static final String ORDER_DESCRIPTION = "苍穹外卖订单";

    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final ShoppingCartMapper shoppingCartMapper;
    private final AddressBookMapper addressBookMapper;
    private final UserMapper userMapper;
    private final WeChatPayUtil weChatPayUtil;

    public OrderServiceImpl(OrderMapper orderMapper,
                            OrderDetailMapper orderDetailMapper,
                            ShoppingCartMapper shoppingCartMapper,
                            AddressBookMapper addressBookMapper,
                            UserMapper userMapper,
                            WeChatPayUtil weChatPayUtil) {
        this.orderMapper = orderMapper;
        this.orderDetailMapper = orderDetailMapper;
        this.shoppingCartMapper = shoppingCartMapper;
        this.addressBookMapper = addressBookMapper;
        this.userMapper = userMapper;
        this.weChatPayUtil = weChatPayUtil;
    }

    /**
     * 用户下单。
     *
     * 下单属于复合写操作：订单主表、订单明细、购物车清空必须在同一个事务中完成。
     */
    @Transactional
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        Long userId = BaseContext.getCurrentId();
        log.info("用户下单开始，userId={}，参数={}", userId, ordersSubmitDTO);

        ShoppingCart queryCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(queryCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new OrderBusinessException("购物车为空");
        }

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new OrderBusinessException("地址不存在");
        }

        LocalDateTime orderTime = LocalDateTime.now();
        BigDecimal goodsAmount = calculateGoodsAmount(shoppingCartList);
        int packAmount = calculatePackAmount(shoppingCartList);
        BigDecimal orderAmount = goodsAmount
                .add(BigDecimal.valueOf(packAmount))
                .add(DELIVERY_FEE);

        Orders orders = Orders.builder()
                .number(String.valueOf(System.currentTimeMillis()))
                .status(Orders.PENDING_PAYMENT)
                .userId(userId)
                .addressBookId(addressBook.getId())
                .orderTime(orderTime)
                .checkoutTime(orderTime)
                .payMethod(ordersSubmitDTO.getPayMethod())
                .payStatus(Orders.UN_PAID)
                .amount(orderAmount)
                .remark(ordersSubmitDTO.getRemark())
                .phone(addressBook.getPhone())
                .address(buildAddress(addressBook))
                .consignee(addressBook.getConsignee())
                .estimatedDeliveryTime(orderTime.plusHours(1))
                .deliveryStatus(ordersSubmitDTO.getDeliveryStatus())
                .packAmount(packAmount)
                .tablewareNumber(ordersSubmitDTO.getTablewareNumber())
                .tablewareStatus(ordersSubmitDTO.getTablewareStatus())
                .build();

        orderMapper.insert(orders);
        log.info("订单主表插入完成，orderId={}，orderNumber={}", orders.getId(), orders.getNumber());

        List<OrderDetail> orderDetailList = buildOrderDetails(shoppingCartList, orders.getId());
        orderDetailMapper.insertBatch(orderDetailList);
        log.info("订单明细插入完成，orderId={}，明细数量={}", orders.getId(), orderDetailList.size());

        shoppingCartMapper.deleteByUserId(userId);
        log.info("用户购物车已清空，userId={}", userId);

        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orderAmount)
                .orderTime(orderTime)
                .build();
    }

    /**
     * 订单支付
     *
     * 作用：
     * 1. 根据当前登录用户获取微信 openid
     * 2. 根据订单号查询订单
     * 3. 校验订单是否存在、是否已支付
     * 4. 调用微信支付生成预支付交易单
     * 5. 返回前端调起微信支付所需的参数
     *
     * @param ordersPaymentDTO 订单支付参数，包含订单号、支付方式等
     * @return 前端调起微信支付所需的参数
     * @throws Exception 微信支付调用、签名、网络请求等过程可能抛出异常
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {

        // 从当前线程上下文中获取当前登录用户 id
        Long userId = BaseContext.getCurrentId();

        // 记录支付开始日志，方便排查支付问题
        log.info("订单支付开始，userId={}，参数={}", userId, ordersPaymentDTO);

        // 根据当前用户 id 查询用户信息，主要是为了获取微信 openid
        User user = userMapper.getById(userId);

        // 校验用户是否存在，以及是否绑定了微信 openid
        if (user == null || user.getOpenid() == null || user.getOpenid().trim().isEmpty()) {
            throw new OrderBusinessException("用户微信身份信息不存在");
        }

        // 根据订单号查询订单
        Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());

        // 校验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException("订单不存在");
        }

        // 校验订单是否已经支付，防止重复支付
        if (Orders.PAID.equals(orders.getPayStatus())) {
            throw new OrderBusinessException("该订单已支付");
        }

        // 调用微信支付工具类，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                orders.getNumber(),       // 商户订单号
                orders.getAmount(),       // 支付金额
                ORDER_DESCRIPTION,        // 订单描述
                user.getOpenid()          // 当前微信用户 openid
        );

        // 如果微信返回 ORDERPAID，说明微信那边认为该订单已经支付
        if ("ORDERPAID".equals(jsonObject.getString("code"))) {
            throw new OrderBusinessException("该订单已支付");
        }

        // 将微信返回的 JSON 支付参数转换成 OrderPaymentVO
        OrderPaymentVO orderPaymentVO = jsonObject.toJavaObject(OrderPaymentVO.class);

        // 微信返回字段叫 package，但 package 是 Java 关键字，
        // 所以手动把 JSON 中的 package 字段赋值给 VO 中的 packageStr
        orderPaymentVO.setPackageStr(jsonObject.getString("package"));

        // 记录预下单完成日志
        log.info("订单支付预下单完成，orderNumber={}，返回={}", orders.getNumber(), orderPaymentVO);

        // 返回前端调起微信支付所需的参数
        return orderPaymentVO;
    }

    /**
     * 支付成功回调：根据订单号修改订单状态。
     */
    @Override
    public void paySuccess(String outTradeNo) {
        log.info("处理微信支付成功回调，orderNumber={}", outTradeNo);

        Orders ordersDB = orderMapper.getByNumber(outTradeNo);
        if (ordersDB == null) {
            throw new OrderBusinessException("订单不存在");
        }

        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        log.info("订单支付状态更新完成，orderId={}，orderNumber={}", ordersDB.getId(), outTradeNo);
    }

    /**
     * 计算购物车商品金额，不包含餐盒费和配送费。
     */
    private BigDecimal calculateGoodsAmount(List<ShoppingCart> shoppingCartList) {
        BigDecimal goodsAmount = BigDecimal.ZERO;
        for (ShoppingCart shoppingCart : shoppingCartList) {
            BigDecimal itemAmount = shoppingCart.getAmount()
                    .multiply(BigDecimal.valueOf(shoppingCart.getNumber()));
            goodsAmount = goodsAmount.add(itemAmount);
        }
        return goodsAmount;
    }

    /**
     * 每份菜品或套餐一个餐盒，每个餐盒 1 元。
     */
    private int calculatePackAmount(List<ShoppingCart> shoppingCartList) {
        int itemCount = 0;
        for (ShoppingCart shoppingCart : shoppingCartList) {
            itemCount += shoppingCart.getNumber();
        }
        return PACK_AMOUNT_UNIT.multiply(BigDecimal.valueOf(itemCount)).intValue();
    }

    /**
     * 将地址簿中的省市区和详细地址拼成订单快照。
     */
    private String buildAddress(AddressBook addressBook) {
        StringBuilder address = new StringBuilder();
        appendIfNotBlank(address, addressBook.getProvinceName());
        appendIfNotBlank(address, addressBook.getCityName());
        appendIfNotBlank(address, addressBook.getDistrictName());
        appendIfNotBlank(address, addressBook.getDetail());
        return address.toString();
    }

    private void appendIfNotBlank(StringBuilder builder, String value) {
        if (value != null && !value.trim().isEmpty()) {
            builder.append(value.trim());
        }
    }

    /**
     * 购物车记录转换为订单明细记录。
     */
    private List<OrderDetail> buildOrderDetails(List<ShoppingCart> shoppingCartList, Long orderId) {
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = OrderDetail.builder()
                    .name(shoppingCart.getName())
                    .orderId(orderId)
                    .dishId(shoppingCart.getDishId())
                    .setmealId(shoppingCart.getSetmealId())
                    .dishFlavor(shoppingCart.getDishFlavor())
                    .number(shoppingCart.getNumber())
                    .amount(shoppingCart.getAmount())
                    .image(shoppingCart.getImage())
                    .build();
            orderDetailList.add(orderDetail);
        }
        return orderDetailList;
    }
}
