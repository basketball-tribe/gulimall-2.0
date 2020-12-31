package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName GuliFeignConfig
 * @Description: 在feign请求中存储cookie避免在服务调用的时候没有携带cookie信息
 * @Author fengjc
 * @Date 2020/12/31
 * @Version V1.0
 **/

/**
 * 在feign的调用过程中，会使用容器中的RequestInterceptor对RequestTemplate进行处理，
 * 因此我们可以通过向容器中导入定制的RequestInterceptor为请求加上cookie。
 */
@Configuration
public class GuliFeignConfig {
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                //1.使用RequestContextHolder拿到老的请求头
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null) {
                    HttpServletRequest request = requestAttributes.getRequest();
                    if (request != null) {
                        //2. 将老请求得到cookie信息放到feign请求上
                        String cookie = request.getHeader("cookie");
                        requestTemplate.header(cookie);
                    }
                }
            }
        };
    }
}
