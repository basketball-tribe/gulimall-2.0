package com.atguigu.gulimall.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/*
* EnableFeignClients引用feign
* EnableDiscoveryClient 注册到nacos服务器
* SpringBootApplication  启动类
* */
@EnableFeignClients(basePackages = {"com.atguigu.gulimall.product.feign"})
@EnableDiscoveryClient
@SpringBootApplication
@EnableRedisHttpSession // 开启session保存到redis中
public class ProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductApplication.class, args);
    }

}
