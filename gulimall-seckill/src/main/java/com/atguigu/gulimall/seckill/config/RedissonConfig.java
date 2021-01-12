package com.atguigu.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName RedissonConfig
 * @Description: redis数据库连接  可以用来设置信号量等
 * @Author fengjc
 * @Date 2021/1/12
 * @Version V1.0
 **/
@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://123.56.152.42:6379");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
