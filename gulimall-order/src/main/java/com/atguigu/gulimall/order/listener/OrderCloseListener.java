package com.atguigu.gulimall.order.listener;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.io.IOException;

/**
 * @ClassName OrderCloseListener
 * @Description: 订单关闭监听
 * @Author fengjc
 * @Date 2021/1/8
 * @Version V1.0
 **/

/**
 * @RabbitListener 标注在类上面表示当有收到消息的时候，
 * 就交给 @RabbitHandler 的方法处理，具体使用哪个方法处理，根据 MessageConverter 转换后的参数类型
 */

/**
 * 创建订单的消息会进入延迟队列，最终发送至队列order.release.order.queue，因此我们对该队列进行监听，进行订单的关闭
 */
@Slf4j
@Component
@RabbitListener(queues = {"order.release.order.queue"})
public class OrderCloseListener {
    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity orderEntity, Message message,Channel channel) throws IOException {
        log.info("************************收到过期的订单信息，准备关闭订单***{}",orderEntity.getOrderSn());
        System.out.println("收到过期的订单信息，准备关闭订单" + orderEntity.getOrderSn());
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        //采用消费者的Ack重试机制，保证消息消费失败之后退回到消息队列中重新消费
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(deliveryTag,false);
        }catch (Exception e){
            channel.basicReject(deliveryTag,true);
        }
    }
}
