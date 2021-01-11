package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.service.SecKillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionWithSkusVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @ClassName SecKillServiceImpl
 * @Description: 秒杀实现类
 * @Author fengjc
 * @Date 2021/1/11
 * @Version V1.0
 **/
public class SecKillServiceImpl implements SecKillService {
    @Autowired
    private CouponFeignService couponFeignService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private ProductFeignService productFeignService;
    /*秒杀的场次信息*/
    //K: SESSION_CACHE_PREFIX + startTime + "_" + endTime
    //V: sessionId+"-"+skuId的List
    private final String SESSION_CACHE_PREFIX = "seckill:sessions:";

    /*秒杀的商品信息*/
    //K: 固定值SECKILL_CHARE_PREFIX
    //V: hash，k为sessionId+"-"+skuId，v为对应的商品信息SeckillSkuRedisTo
    private final String SECKILL_CHARE_PREFIX = "seckill:skus";

    /*秒杀的库存信息*/
    //K: SKU_STOCK_SEMAPHORE+商品随机码
    //V: 秒杀的库存件数
    private final String SKU_STOCK_SEMAPHORE = "seckill:stock:";    //+商品随机码
    @Override
    public void uploadSeckillSkuLatest3Days() {
        R r = couponFeignService.getSeckillSessionsIn3Days();
        if (r.getCode() == 0){
            List<SeckillSessionWithSkusVo> sessions = r.getData(new TypeReference<List<SeckillSessionWithSkusVo>>() {
            });
            //在redis中分别保存秒杀场次信息和场次对应的秒杀商品信息
            saveSecKillSession(sessions);
            saveSecKillSku(sessions);
        }
    }

    /**
     * 存入秒杀商品信息
     * @param sessions
     */
    private void saveSecKillSku(List<SeckillSessionWithSkusVo> sessions) {
        //以map的形式存储信息
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
        sessions.stream().forEach(session ->{
            session.getRelations().stream().forEach(sku->{
                String key = sku.getPromotionSessionId() +"-"+ sku.getSkuId();//key为商品活动场次id+商品id
                if(!ops.hasKey(key)){
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    //1. 保存SeckillSkuVo信息
                    BeanUtils.copyProperties(sku,redisTo);
                    //2. 保存开始结束时间
                    redisTo.setStartTime(session.getStartTime().getTime());
                    redisTo.setEndTime(session.getEndTime().getTime());
                    //3. 远程查询sku信息并保存
                    R r = productFeignService.info(sku.getSkuId());
                    if (r.getCode() == 0) {
                        SkuInfoVo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                        });
                        redisTo.setSkuInfoVo(skuInfo);
                    }
                    //4. 生成商品随机码，防止恶意攻击
                    String token = UUID.randomUUID().toString().replace("-", "");
                    redisTo.setRandomCode(token);
                    //5. 序列化为json并保存
                    String jsonString = JSON.toJSONString(redisTo);
                    //key =商品场次+商品id
                    //v = 商品详细信息
                    ops.put(key,jsonString);
                    //6. 使用库存作为Redisson信号量限制库存
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisTo.getRandomCode());
                    semaphore.trySetPermits(sku.getSeckillCount());
                }
            });
        });
    }

    /**
     * 存入秒杀场次信息
     * @param sessions
     */
    private void saveSecKillSession(List<SeckillSessionWithSkusVo> sessions) {
        sessions.stream().forEach(session ->{
            String key = SESSION_CACHE_PREFIX + session.getStartTime().getTime() + "_" + session.getEndTime().getTime();
            //如果不存在再存入redis中
            if(!redisTemplate.hasKey(key)){
                List<String> values = session.getRelations().stream()
                        .map(sku -> sku.getPromotionSessionId() + "-" + sku.getSkuId())
                        .collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key,values);
            }
        });
    }
}
