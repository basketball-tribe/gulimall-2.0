package com.atguigu.gulimall.order.feign;

import com.atguigu.gulimall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     * 远程查询商品信息
     * @param skuId
     * @return
     */
    @RequestMapping("product/spuinfo/skuId/{skuId}")
    R getSpuBySkuId(@PathVariable("skuId") Long skuId);

    @RequestMapping("product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);
}
