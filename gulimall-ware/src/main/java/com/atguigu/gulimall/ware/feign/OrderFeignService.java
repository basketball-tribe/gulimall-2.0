package com.atguigu.gulimall.ware.feign;

import com.atguigu.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @ClassName OrderFeignService
 * @Description: 调用订单服务feign
 * @Author fengjc
 * @Date 2021/1/8
 * @Version V1.0
 **/
@FeignClient("gulimall-order")
public interface OrderFeignService {
    @RequestMapping("order/order/infoByOrderSn/{OrderSn}")
    R infoByOrderSn(String orderSn);
}
