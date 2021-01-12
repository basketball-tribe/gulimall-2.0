package com.atguigu.gulimall.seckill.service;

import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;

import java.util.List;

/**
 * @ClassName SecKillService
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/11
 * @Version V1.0
 **/
public interface SecKillService {
    void uploadSeckillSkuLatest3Days();

    /**
     * 当前时间是否有秒杀活动
     * @return
     */
    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    /**
     * 当前时间的秒杀商品信息
     * @param skuId
     * @return
     */
    SeckillSkuRedisTo getSeckillSkuInfo(Long skuId);

    /**
     * 秒杀
     * @param killId
     * @param key
     * @param num
     * @return
     */
    String kill(String killId, String key, Integer num) throws InterruptedException;
}
