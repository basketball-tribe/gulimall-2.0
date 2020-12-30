package com.atguigu.gulimall.cart.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @ClassName GulimallWebConfig
 * @Description: 拦截器
 * @Author fengjc
 * @Date 2020/12/30
 * @Version V1.0
 **/
@Configuration
public class GulimallWebConfig implements  WebMvcConfigurer {
    @Override
    public void  addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CartInterceptor()).addPathPatterns("/**");
    }
}
