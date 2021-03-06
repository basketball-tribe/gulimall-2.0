package com.atguigu.gulimall.ware.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * json转换器
 */
@Configuration
public class MyAmqpConfig {
    @Bean
    public MessageConverter messageConverter() {
        //在容器中导入Json的消息转换器
        return new Jackson2JsonMessageConverter();
    }
}
