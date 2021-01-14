package com.atguigu.gulimall.product.feign.fallback;

import com.atguigu.gulimall.common.utils.BizCodeEnum;
import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.product.feign.SeckillFeignService;
import org.springframework.stereotype.Component;

/**
 * @ClassName SeckillFallbackService
 * @Description: sentinel降级处理类
 * @Author fengjc
 * @Date 2021/1/14
 * @Version V1.0
 **/
@Component
public class SeckillFallbackService implements SeckillFeignService {
    @Override
    public R getSeckillSkuInfo(Long skuId) {
        return R.error(BizCodeEnum.READ_TIME_OUT_EXCEPTION.getCode(), BizCodeEnum.READ_TIME_OUT_EXCEPTION.getMsg());

    }

    @Override
    public R getCurrentSeckillSkus() {
        return R.error(BizCodeEnum.READ_TIME_OUT_EXCEPTION.getCode(), BizCodeEnum.READ_TIME_OUT_EXCEPTION.getMsg());
    }

}
