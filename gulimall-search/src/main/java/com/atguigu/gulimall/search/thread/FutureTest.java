package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @ClassName FutureTest
 * @Description: 实现了在5秒内若没有调用成功则抛出异常
 * @Author fengjc
 * @Date 2021/1/28
 * @Version V1.0
 **/
public class FutureTest {

    public static void main(String[] args) {
        try {
            test();
            System.out.println("正常调用");
        } catch (Exception e) {
            System.out.println("调用超时");
        }
    }

    public static void test() throws InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<Boolean> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                //TODO 执行其他业务逻辑，模拟执行了2秒
                Thread.sleep(4000);
                System.out.println("2秒后");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        });
        //若在5秒内没有响应则则抛出TimeoutException异常
        if (completableFuture.get(5, TimeUnit.SECONDS)) {
            System.out.println("success");
        } else {
            System.out.println("false");
        }
    }
}
