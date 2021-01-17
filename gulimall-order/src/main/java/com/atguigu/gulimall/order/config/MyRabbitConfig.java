package com.atguigu.gulimall.order.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
/**
 * @ClassName MyRabbitConfig
 * @Description: rabbitmq
 * @Author fengjc
 * @Date 2021/1/7
 * @Version V1.0
 **/

/**
 * 延迟队列
 * 延迟队列存储的对象肯定是对应的延时消息，所谓"延时消息"是指当消息被发送以后，并不想让消费者立即拿到消息，
 * 而是等待指定时间后，消费者才拿到这个消息进行消费
 * 1.创建一个交换机，两个队列（1个普通队列，没有监听 1个由死信路由转发的队列，监听普通队列中过期的消息）
 *2.创建订单时消息会被发送至队列order.delay.queue，经过TTL的时间后消息会变成死信以order.release.order的路由键
 * 经交换机转发至队列order.release.order.queue，再通过监听该队列的消息来实现过期订单的处理
 */
@Configuration
public class MyRabbitConfig {
    /**
     * 创建一个普通的交换机
     * @return
     */
    @Bean
    public Exchange orderEventExchange(){
        /**
         * String name,交换机名字
         * boolean durable,持久
         * boolean autoDelete,自动删除
         * Map<String, Object> arguments
         */
        return new TopicExchange("order-event-exchange",true,false);
    }

    /**
     * 延迟队列
     *创建订单时将消息发送到该队列，该队列经过TTL到期后会变成死信，以order.release.order路由键经
     * @return
     */
    @Bean
    public Queue orderDelayQueue(){
        /**
         * String name, 队列名称
         * boolean durable, 持久化
         * boolean exclusive,排他
         * boolean autoDelete, 自删
         * Map<String, Object> arguments 自定义参数
         */
        Map<String,Object> arg =new HashMap<>();
        //死信交换机
        arg.put("x-dead-letter-exchange","order-event-exchange");
        //死信路由键
        arg.put("x-dead-letter-routing-key","order.release.order");
        //过期时间
        arg.put("x-message-ttl",60000);//消息过期时间 1分钟
        return new Queue("order.delay.queue",true,false,false);
    }

    /**
     * 创建一个普通队列订单解锁队列
     * @return
     */
    @Bean
    public Queue orderReleaseQueue(){
        return  new Queue("order.release.order.queue",true,false,false);
    }

    /**
     * 商品秒杀队列
     * @return
     */
    @Bean
    public Queue orderSecKillOrrderQueue() {
        Queue queue = new Queue("order.seckill.order.queue", true, false, false);
        return queue;
    }
    /**
     * 创建订单的binding
     * 发送消息到延迟队列
     * @return
     */
    @Bean
    public Binding orderCreateBinding(){
        /**
         * String destination, 目标
         * Binding.DestinationType destinationType, 目标类型
         * String exchange,交换机
         * String routingKey,路由键
         * Map<String, Object> arguments 参数
         */
    return new Binding("order.delay.queue",
            Binding.DestinationType.QUEUE,
            "order-event-exchange",
            "order.create.order",null);
    }

    /**
     * 创建消费延迟消息的Binding
     * 从延迟队列到普通队列
     * @return
     */
    @Bean
    public Binding orderReleaseBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",null);
    }
    @Bean
    public Binding orderReleaseOrderBinding(){
        return  new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }
    @Bean
    public Binding orderSecKillOrrderQueueBinding(){
        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }
}
