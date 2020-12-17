package com.atguigu.gulimall.product.test;

import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName TestRedissonController
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/17
 * @Version V1.0
 **/
@RestController
@RequestMapping("product")
public class TestRedissonController {
    @Autowired
    RedissonClient redissonClient;
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 可重入锁
     * @return
     */
    @GetMapping("/reentrant")
    public String reentrant(){
        RLock lock = redissonClient.getLock("CatalogJson-Lock");
        lock.lock();
        //Redisson还通过加锁的方法提供了leaseTime的参数来指定加锁的时间。超过这个时间后锁便自动解开了。不会自动续期！
  //      lock.lock(10, TimeUnit.SECONDS);
       // boolean res = lock.tryLock(100, 10, TimeUnit.SECONDS);

        try {
            Thread.sleep(30000);
          System.out.println("重入锁");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
            return "完成";
        }
    }

    @GetMapping("/read")
    public String read() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("ReadWrite-Lock");
        RLock rLock = lock.readLock();
        String s = "";
        try {
            rLock.lock();
            System.out.println("读锁加锁"+Thread.currentThread().getId());
            Thread.sleep(5000);
            s= redisTemplate.opsForValue().get("lock-value");
        }finally {
            rLock.unlock();
            return "读取完成:"+s;
        }
    }

    @GetMapping("/write")
    public String write() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("ReadWrite-Lock");
        RLock wLock = lock.writeLock();
        String s = UUID.randomUUID().toString();
        try {
            wLock.lock();
            System.out.println("写锁加锁"+Thread.currentThread().getId());
            Thread.sleep(10000);
            redisTemplate.opsForValue().set("lock-value",s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            wLock.unlock();
            return "写入完成:"+s;
        }
    }
    /**
     * 信号量为存储在redis中的一个数字，当这个数字大于0时，即可以调用`acquire()`方法增加数量，也可以调用`release()`方法减少数量
     * ，但是当调用`release()`之后小于0的话方法就会阻塞，直到数字大于0
     * @return
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() {
        RSemaphore park = redissonClient.getSemaphore("park");
        try {
            park.acquire(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "停进2";
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release(2);
        return "开走2";
    }

    /**
     * 闭锁（CountDownLatch）
     *
     * 可以理解为门栓，使用若干个门栓将当前方法阻塞，只有当全部门栓都被放开时，当前方法才能继续执行。
     *
     * 以下代码只有offLatch()被调用5次后 setLatch()才能继续执行
     * @return
     */
    @GetMapping("/setLatch")
    @ResponseBody
    public String setLatch() {
        RCountDownLatch latch = redissonClient.getCountDownLatch("CountDownLatch");
        try {
            latch.trySetCount(5);
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "门栓被放开";
    }

    @GetMapping("/offLatch")
    @ResponseBody
    public String offLatch() {
        RCountDownLatch latch = redissonClient.getCountDownLatch("CountDownLatch");
        latch.countDown();
        return "门栓被放开1";
    }
}
