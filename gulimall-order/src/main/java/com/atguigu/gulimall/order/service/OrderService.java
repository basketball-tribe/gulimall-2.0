package com.atguigu.gulimall.order.service;

import com.atguigu.gulimall.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.common.utils.PageUtils;
import com.atguigu.gulimall.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-10 18:34:05
 */
public interface OrderService extends IService<OrderEntity> {


    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo);

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    PageUtils getMemberOrderPage(Map<String, Object> params);

    PayVo getOrderPay(String orderSn);

    void handlerPayResult(PayAsyncVo payAsyncVo);

    void createSeckillOrder(SeckillOrderTo orderTo);

}

