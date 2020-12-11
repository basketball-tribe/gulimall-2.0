package com.atguigu.gulimall.member;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 远程调用步骤
 * 1.引入open-feign
 * 2.编写一个接口，告诉springcloud这个接口需要调用远程服务
 * 3.声明接口的每一个方法都是调用哪个远程服务的那个请求
 * 4.开启远程调用功能
 * 5.feign默认实现了负载均衡
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class MemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }

}
