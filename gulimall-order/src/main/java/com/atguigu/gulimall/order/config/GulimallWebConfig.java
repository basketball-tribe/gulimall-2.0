package com.atguigu.gulimall.order.config;

import com.atguigu.gulimall.order.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @ClassName GulimallWebConfig
 * @Description: 订单服务登录拦截器
 * @Author fengjc
 * @Date 2020/12/30
 * @Version V1.0
 **/
@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {
    @Override
    public  void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**");
    }
}
