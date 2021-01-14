package com.atguigu.gulimall.product.feign;

import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.product.feign.fallback.SeckillFallbackService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName SeckillFeignService
 * @Description: 秒杀服务feign
 * @Author fengjc
 * @Date 2021/1/14
 * @Version V1.0
 **/
@FeignClient(value = "gulimall-seckill",fallback = SeckillFallbackService.class)
public interface SeckillFeignService {
    @ResponseBody
    @GetMapping(value = "/getSeckillSkuInfo/{skuId}")
    R getSeckillSkuInfo(Long skuId);

    @ResponseBody
    @GetMapping(value = "/getCurrentSeckillSkus")
    R getCurrentSeckillSkus();
}
