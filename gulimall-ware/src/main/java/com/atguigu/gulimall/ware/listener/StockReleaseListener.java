package com.atguigu.gulimall.ware.listener;

import com.atguigu.gulimall.common.to.mq.OrderTo;
import com.atguigu.gulimall.common.to.mq.StockLockedTo;
import com.atguigu.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @ClassName StockReleaseListener
 * @Description: 解锁库存监听类
 * @Author fengjc
 * @Date 2021/1/8
 * @Version V1.0
 **/
@Slf4j
@Component
@RabbitListener(queues = {"stock.release.stock.queue"})
public class StockReleaseListener {
    @Autowired
    private WareSkuService wareSkuService;

    /**
     * 监控从库存服务中来的解锁库存的消息
     * @param stockLockedTo
     * @param message
     * @param channel
     * @throws IOException
     */
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {
        log.info("************************收到库存解锁的消息********************************");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            wareSkuService.unlock(stockLockedTo);
            channel.basicAck(deliveryTag,false);
        }catch (Exception e){
            channel.basicReject(deliveryTag,true);
        }

    }

    /**
     * 监控从订单服务来的解库存的消息，订单关闭后发送的消息队列
     * @param orderTo
     * @param message
     * @param channel
     */
    @RabbitHandler
    public void  handleOrderLockedRelease(OrderTo orderTo,Message message, Channel channel) throws IOException {
        log.info("************************从订单模块收到库存解锁的消息********************************");
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            wareSkuService.unlock(orderTo);
            channel.basicAck(deliveryTag,false);
        }catch (Exception e){
            channel.basicReject(deliveryTag,true);
        }
    }
}
