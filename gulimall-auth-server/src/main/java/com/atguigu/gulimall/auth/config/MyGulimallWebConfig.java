package com.atguigu.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * User: haitao
 * Date: 2020/7/9
 */
@Configuration
public class MyGulimallWebConfig implements WebMvcConfigurer {
    /**
     * 发送请求直接获取页面，不需要业务逻辑，我们可以使用SpringMVC 提供的ViewController实现
     * 视图映射
     * @param registry
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }

}
