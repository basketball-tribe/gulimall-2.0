package com.atguigu.gulimall.product.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName LockTest
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/22
 * @Version V1.0
 **/
public class LockTest {
    @Autowired
    private static RedisTemplate redisTemplate;

    public static void main(String[] args) {
        getList();
    }

    private static void getList() {
        getListLock();
    }

    private static List<TestEntity> getListLock() {
        //1.生成一个uuid作为锁放入redis中只有获取这个锁的服务器才能执行方法
        String uuid = UUID.randomUUID().toString();
        //setIfAbsent(),如果为空就set值，并返回1
        //如果存在(不为空)不进行操作，并返回0
        boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 5, TimeUnit.SECONDS);
        if (lock) {
            List<TestEntity> testEntities = getListFromRedis();
            String lockValue = (String) redisTemplate.opsForValue().get("lock");
            //获取到数据就删除掉redis的lock
            //删除一定是原子性操作
            String scipt = "if redis.call(\"get\",KEYS[1])== ARGV[1] then" +
                    " return redis.call(\"del\",KEYS[1])" +
                    " else " +
                    "  return 0" +
                    "end";
            redisTemplate.execute(new DefaultRedisScript(scipt, Long.class), Arrays.asList("lock"), lockValue);
            return testEntities;
        } else {
            try {
                //停留0.1s再进行调用查询
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return getListLock();
        }
    }

    private static List<TestEntity> getListFromRedis() {
        String redisResult = (String) redisTemplate.opsForValue().get("TestEntitys");
        if (StringUtils.isEmpty(redisResult)) {
            //加锁后查询前再进行查询一次
            //此处为静态方法不能使用锁，在实际运用中可以用
          //  synchronized (this) {
                String redisResult2 = (String) redisTemplate.opsForValue().get("TestEntitys");
                if (StringUtils.isEmpty(redisResult2)) {
                    //此处是从数据库中查询数据
                    List<TestEntity> testEntities3 = new ArrayList<>();
                    return testEntities3;
                } else {
                    List<TestEntity> testEntities2 = JSON.parseObject(redisResult2, new TypeReference<List<TestEntity>>() {
                    });
                    return testEntities2;
                }
        //    }


        } else {
            List<TestEntity> testEntities = JSON.parseObject(redisResult, new TypeReference<List<TestEntity>>() {
            });
            return testEntities;
        }

    }
}
