package com.atguigu.gulimall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.common.vo.MemberResponseVo;
import com.atguigu.gulimall.seckill.feign.CouponFeignService;
import com.atguigu.gulimall.seckill.feign.ProductFeignService;
import com.atguigu.gulimall.seckill.interceptor.LoginInterceptor;
import com.atguigu.gulimall.seckill.service.SecKillService;
import com.atguigu.gulimall.seckill.to.SeckillSkuRedisTo;
import com.atguigu.gulimall.seckill.vo.SeckillSessionWithSkusVo;
import com.atguigu.gulimall.seckill.vo.SkuInfoVo;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
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
        if (r.getCode() == 0) {
            List<SeckillSessionWithSkusVo> sessions = r.getData(new TypeReference<List<SeckillSessionWithSkusVo>>() {
            });
            //在redis中分别保存秒杀场次信息和场次对应的秒杀商品信息
            saveSecKillSession(sessions);
            saveSecKillSku(sessions);
        }
    }

    /**
     * 当前时间是否有秒杀活动
     *
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        //取出redis中的缓存的场次信息的key
        Set<String> keys = redisTemplate.keys(SESSION_CACHE_PREFIX + "*");
        //当前时间
        long currentTime = System.currentTimeMillis();
        for (String key : keys) {
            //处理key信息截取到场次的开始时间和结束时间
            String replace = key.replace(SESSION_CACHE_PREFIX, "");
            String[] split = replace.split("_");
            long startTime = Long.parseLong(split[0]);
            long endTime = Long.parseLong(split[1]);
            //如果当前时间在秒杀场次时间内则查询秒杀信息
            if (currentTime >= startTime && currentTime <= endTime) {
                //将redis中商品信息取出
                //获取商品信息的key
                List<String> range = redisTemplate.opsForList().range(key, -100, 100);
                BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
                List<SeckillSkuRedisTo> seckillSkuRedisTos = range.stream().map(item -> {
                    String json = (String) ops.get(item);
                    SeckillSkuRedisTo seckillSkuRedisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                    return seckillSkuRedisTo;
                }).collect(Collectors.toList());
                return seckillSkuRedisTos;
            }
        }
        return null;
    }

    /**
     * 从redis中获取当前时间的秒杀商品信息
     *
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo getSeckillSkuInfo(Long skuId) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
        Set<String> keys = ops.keys();
        for (String key : keys) {
            if (Pattern.matches("\\d-" + skuId, key)) {
                String json = ops.get(key);
                SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
                //当前商品参与秒杀活动
                if (redisTo != null) {
                    long current = System.currentTimeMillis();
                    //在秒杀活动时间内则返回商品信息
                    //当前活动在有效期，暴露商品随机码返回
                    if (current >= redisTo.getStartTime() && current <= redisTo.getEndTime()) {
                        return redisTo;
                    }
                    //不在秒杀活动时间清除随机码
                    redisTo.setRandomCode(null);
                    return redisTo;
                }
            }


        }
        return null;
    }

    /**
     * 秒杀
     *
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String kill(String killId, String key, Integer num) throws InterruptedException {
        //1. 验证时效
        //2. 验证商品和商品随机码是否对应
        //3.验证当前用户是否购买过
        //4.检验库存和购买量是否符合要求
        //5.发送消息创建订单
        //获取redis中存储的map信息
        BoundHashOperations ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
        String json = (String) ops.get(killId);
        String orderSn = null;
        if (!StringUtils.isEmpty(json)) {
            SeckillSkuRedisTo redisTo = JSON.parseObject(json, SeckillSkuRedisTo.class);
            //1.验证时效
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis >= redisTo.getStartTime() && currentTimeMillis <= redisTo.getEndTime()) {
                //2验证商品和随机码是否对应
                String redisKey = redisTo.getPromotionSessionId() + "_" + redisTo.getSkuId();
                if (redisKey.equals(killId) && redisTo.getRandomCode().equals(key)) {
                    //3验证当前用户是否购买过
                    MemberResponseVo memberResponseVo = LoginInterceptor.loginUser.get();
                    long ttl = redisTo.getEndTime() - System.currentTimeMillis();
                    //3.1 通过在redis中使用 用户id-skuId 来占位看是否买过
                    //如果键不存在则新增,存在则不改变已经有的值。
                    //k 用户id+商品id
                    //v 购买数量
                    //有效期为 秒杀结束的时间-当前时间
                    Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(memberResponseVo.getId() + "_" + redisTo.getSkuId(), num, ttl, TimeUnit.SECONDS);
                    //3.2 占位成功，说明该用户未秒杀过该商品，则继续
                    //如果购买过则 aBoolean 为false 新增为true
                    if(aBoolean){
                        //4.检验库存和购买量是否符合要求
                        if(num<redisTo.getSeckillLimit()){
                            //4.1 尝试获取库存信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisTo.getRandomCode());
                            boolean acquire = semaphore.tryAcquire(num, 100, TimeUnit.MILLISECONDS);
                            //4.2 获取库存成功
                            if(acquire){
                                //5. 发送消息创建订单
                                //5.1 创建订单号
                                orderSn = IdWorker.getTimeId();

                            }
                        }
                    }
                }
            }

        }

        return null;
    }

    /**
     * 存入秒杀商品信息
     *
     * @param sessions
     */
    private void saveSecKillSku(List<SeckillSessionWithSkusVo> sessions) {
        //以map的形式存储信息
        BoundHashOperations<String, Object, Object> ops = redisTemplate.boundHashOps(SECKILL_CHARE_PREFIX);
        sessions.stream().forEach(session -> {
            session.getRelations().stream().forEach(sku -> {
                String key = sku.getPromotionSessionId() + "-" + sku.getSkuId();//key为商品活动场次id+商品id
                if (!ops.hasKey(key)) {
                    SeckillSkuRedisTo redisTo = new SeckillSkuRedisTo();
                    //1. 保存SeckillSkuVo信息
                    BeanUtils.copyProperties(sku, redisTo);
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
                    ops.put(key, jsonString);
                    //6. 使用库存作为Redisson信号量限制库存
                    RSemaphore semaphore = redissonClient.getSemaphore(SKU_STOCK_SEMAPHORE + redisTo.getRandomCode());
                    semaphore.trySetPermits(sku.getSeckillCount());
                }
            });
        });
    }

    /**
     * 存入秒杀场次信息
     *
     * @param sessions
     */
    private void saveSecKillSession(List<SeckillSessionWithSkusVo> sessions) {
        sessions.stream().forEach(session -> {
            String key = SESSION_CACHE_PREFIX + session.getStartTime().getTime() + "_" + session.getEndTime().getTime();
            //如果不存在再存入redis中
            if (!redisTemplate.hasKey(key)) {
                List<String> values = session.getRelations().stream()
                        .map(sku -> sku.getPromotionSessionId() + "-" + sku.getSkuId())
                        .collect(Collectors.toList());
                redisTemplate.opsForList().leftPushAll(key, values);
            }
        });
    }
}
