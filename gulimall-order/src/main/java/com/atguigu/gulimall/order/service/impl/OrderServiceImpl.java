package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.vo.*;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.common.utils.PageUtils;
import com.atguigu.gulimall.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单获取数据
      * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() {

        return null;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        return null;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return null;
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {

    }

    @Override
    public PageUtils getMemberOrderPage(Map<String, Object> params) {
        return null;
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        return null;
    }

    @Override
    public void handlerPayResult(PayAsyncVo payAsyncVo) {

    }

    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {

    }

}
