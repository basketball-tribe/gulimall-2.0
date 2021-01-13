package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.common.to.mq.OrderTo;
import com.atguigu.gulimall.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.io.IOException;

/**
 * @ClassName SeckillOrderListener
 * @Description: 秒杀订单监听
 * @Author fengjc
 * @Date 2021/1/13
 * @Version V1.0
 **/
@Component
@RabbitListener(queues = "order.seckill.order.queue")
public class SeckillOrderListener {
    @Autowired
    OrderService orderService;
    @RabbitHandler
    public void createOrder(SeckillOrderTo orderTo, Message message, Channel channel) throws IOException {
        System.out.println("***********接收到秒杀消息");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
            try{
                orderService.createSeckillOrder(orderTo);
                channel.basicAck(deliveryTag,false);
            }catch (Exception e){
                channel.basicReject(deliveryTag,true);
            }
    }
}
