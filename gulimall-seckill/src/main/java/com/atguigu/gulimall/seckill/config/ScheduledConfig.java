package com.atguigu.gulimall.seckill.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @ClassName ScheduledConfig
 * @Description: 定时器
 * @Author fengjc
 * @Date 2021/1/11
 * @Version V1.0
 **/
@EnableAsync //开启对异步的支持，防止定时任务之间相互阻塞
@EnableScheduling //开启定时任务
@Configuration
public class ScheduledConfig {
}
