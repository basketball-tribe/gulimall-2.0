package com.atguigu.gulimall.search.thread;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;
import java.util.concurrent.*;

/**
 * @ClassName ThreadTest
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/25
 * @Version V1.0
 **/
public class ThreadTest {
    @Autowired
    private static ExecutorService service = Executors.newFixedThreadPool(5);

    public static void main(String[] args) throws Exception {
      //  runAsync();

//        CompletableFuture<Long> longCompletableFuture = supplyAsync();
//        Long aLong = longCompletableFuture.get();
//        System.out.println(aLong);

//        whenComplete();

//        thenApply();
//        handle();
//        thenRun();
//        thenCombine();
//        thenAcceptBoth();
//        acceptEither();
//        applyToEither();
        allOf();
        System.out.println("主线程执行完成");

    }

    public static void test1(){
        CompletableFuture<Void> future =CompletableFuture.runAsync(()->{
            try {
                Thread.sleep(1000);
                System.out.println("方法1已经执行");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        },service);
        CompletableFuture<Void> future2 =CompletableFuture.runAsync(()->{
            System.out.println("方法2已经执行");
        },service);
    }
    //无返回值
    public static CompletableFuture<Void>  runAsync(){
    CompletableFuture<Void>future =CompletableFuture.runAsync(()->{
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
        }
        System.out.println("run end ..."+Thread.currentThread().getId());
    },service);
    return future;
    }

    //有返回值
    public static CompletableFuture<Long> supplyAsync() throws Exception {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
            }
            System.out.println("run end ...");
            return System.currentTimeMillis();
        });

        return future;
    }

    public static void  whenComplete() throws ExecutionException, InterruptedException {
        CompletableFuture<Long> future =CompletableFuture.supplyAsync(()->{
            try {
                System.out.println("run start ...");
                TimeUnit.SECONDS.sleep(1);
                Long i = 1000L / 0;
            } catch (InterruptedException e) {
            }
           return 1L;
        },service).whenCompleteAsync((res,exception) ->{
            System.out.println("结果完成"+res);
            System.out.println("出现异常:"+exception);
        }).exceptionally(t->{
            System.out.println(t.getMessage());
            return 10L;
        });
        System.out.println("run end ..." +future.get());

    }

    private static void thenApply() throws Exception {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("run start ...");
                TimeUnit.SECONDS.sleep(1);
                Long i = 1000L / 3;
            } catch (InterruptedException e) {
            }
            return 10L;
        }, service).thenApplyAsync(res -> res * 2);
        System.out.println(future.get());
    }

    private static void handle() throws Exception {
        CompletableFuture<Long> future = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("run start ...");
                TimeUnit.SECONDS.sleep(1);
                Long i = 1000L / 0;
                return 1L;
            } catch (Exception e) {
                return 2L;
            }
        }, service).handle((t,u)->{
            System.out.println(u.getMessage());
            return t * 2;
        });
    }

    private static void thenAccept() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("run start ...");
                TimeUnit.SECONDS.sleep(1);
                Long i = 1000L / 3;
                return 1L;
            } catch (InterruptedException e) {
                return 2L;
            }
        }, service).thenAccept(res -> {
            System.out.println(res);
        });
//        System.out.println(future.get());
    }

    private static void thenRun() throws Exception {
        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
            try {
                System.out.println("run start ...");
                TimeUnit.SECONDS.sleep(1);
                Long i = 1000L / 0;
                return 1L;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return -1L;
            }
        }, service).thenRun(() -> {
            System.out.println("thenRun方法执行了，，");
        });
        System.out.println(future.get());
    }
    private static void thenCombine() throws Exception {
        CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "hello1");
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "hello2");
        CompletableFuture<String> result = future1.thenCombine(future2, (t, u) -> t+" "+u);
        System.out.println(result.get());
    }
    private static void thenAcceptBoth() throws Exception {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            int t = new Random().nextInt(3);
            try {
                TimeUnit.SECONDS.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("f1="+t);
            return t;
        },service);

        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            int t = new Random().nextInt(3);
            try {
                TimeUnit.SECONDS.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("f2="+t);
            return t;
        },service);
    }

    private static void applyToEither() throws Exception {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            int t = new Random().nextInt(3);
            try {
                TimeUnit.SECONDS.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("f1="+t);
            return t;
        },service);
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            int t = new Random().nextInt(3);
            try {
                TimeUnit.SECONDS.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("f2="+t);
            return t;
        },service);

        CompletableFuture<Integer> result = f1.applyToEither(f2, t -> {
            System.out.println("applyEither:"+t);
            return t * 2;
        });

    }

    private static void acceptEither() throws Exception {
        CompletableFuture<Integer> f1 = CompletableFuture.supplyAsync(() -> {
            int t = new Random().nextInt(3);
            try {
                TimeUnit.SECONDS.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("f1="+t);
            return t;
        },service);
        CompletableFuture<Integer> f2 = CompletableFuture.supplyAsync(() -> {
            int t = new Random().nextInt(3);
            try {
                TimeUnit.SECONDS.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("f2="+t);
            return t;
        },service);

        CompletableFuture<Void> result = f1.acceptEither(f2, t -> {
            System.out.println("acceptEither:"+t);
        });

    }


    public static void allOf(){
        CompletableFuture<Void> future1 = CompletableFuture.runAsync(()->{
            int t = new Random().nextInt(5);
            try {
                TimeUnit.SECONDS.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("future1"+t);
        });
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(()->{
            int t = new Random().nextInt(5);
            try {
                TimeUnit.SECONDS.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("future2"+t);
        });
        try {
            CompletableFuture.allOf(future1, future2).get();
            System.out.println("都执行完毕");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    /*
    * ### 2.CompletableFuture组合式异步编程

#### (1)  runAsync 和 supplyAsync方法

CompletableFuture 提供了四个静态方法来创建一个异步操作。

```java
public static CompletableFuture<Void> runAsync(Runnable runnable)
public static CompletableFuture<Void> runAsync(Runnable runnable, Executor executor)
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier)
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier, Executor executor)
```

没有指定Executor的方法会使用ForkJoinPool.commonPool() 作为它的线程池执行异步代码。如果指定线程池，则使用指定的线程池运行。以下所有的方法都类同。

- runAsync方法不支持返回值。
- supplyAsync可以支持返回值。

#### (2)  线程串行化

* thenRun：不能获取上一步的执行结果
* thenAcceptAsync：能接受上一步结果，但是无返回值
* thenApplyAsync：能接受上一步结果，有返回值

#### (4) 计算结果完成时的回调方法

当CompletableFuture的计算结果完成，或者抛出异常的时候，可以执行特定的Action。主要是下面的方法：

```java
//可以处理异常，无返回值
public CompletableFuture<T> whenComplete(BiConsumer<? super T,? super Throwable> action)
public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super Throwable> action)
public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super Throwable> action, Executor executor)
//可以处理异常，有返回值
public CompletableFuture<T> exceptionally(Function<Throwable,? extends T> fn)
```

可以看到Action的类型是BiConsumer<? super T,? super Throwable>它可以处理正常的计算结果，或者异常情况。

#### (4) handle 方法

handle 是执行任务完成时对结果的处理。
 handle 方法和 thenApply 方法处理方式基本一样。不同的是 handle 是在任务完成后再执行，还可以处理异常的任务。thenApply 只可以执行正常的任务，任务出现异常则不执行 thenApply 方法。

```java
public <U> CompletionStage<U> handle(BiFunction<? super T, Throwable, ? extends U> fn);
public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn);
public <U> CompletionStage<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn,Execut
```

#### (5) thenCombine 合并任务

thenCombine 会把 两个 CompletionStage 的任务都执行完成后，把两个任务的结果一块交给 thenCombine 来处理。

#### (6) 组合任务

* thenCombine 会把 两个 CompletionStage 的任务都执行完成后，把两个任务的结果一块交给 thenCombine 来处理。

```java
public <U,V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
public <U,V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn);
public <U,V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super T,? super U,? extends V> fn,Executor executor);
```

* thenAcceptBoth

当两个CompletionStage都执行完成后，把结果一块交给thenAcceptBoth来进行消耗

```java
public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action);
public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action);
public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,BiConsumer<? super T, ? super U> action,     Executor executor);
```

* applyToEither 方法

两个CompletionStage，谁执行返回的结果快，我就用那个CompletionStage的结果进行下一步的转化操作。

```java
public <U> CompletionStage<U> applyToEither(CompletionStage<? extends T> other,Function<? super T, U> fn);
public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? super T, U> fn);
public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends T> other,Function<? sup
```

* acceptEither 方法

两个CompletionStage，谁执行返回的结果快，我就用那个CompletionStage的结果进行下一步的消耗操作。

```java
public CompletionStage<Void> acceptEither(CompletionStage<? extends T> other,Consumer<? super T> action);
public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other,Consumer<? super T> action);
public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends T> other,Consumer<? supe
```

* runAfterEither 方法

两个CompletionStage，任何一个完成了都会执行下一步的操作（Runnable）

```java
public CompletionStage<Void> runAfterEither(CompletionStage<?> other,Runnable action);
public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other,Runnable action);
public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other,Runnable action,Executor executor);
```

* runAfterBoth

两个CompletionStage，都完成了计算才会执行下一步的操作（Runnable）

```java
public CompletionStage<Void> runAfterBoth(CompletionStage<?> other,Runnable action);
public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,Runnable action);
public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,Runnable action,Executor
```

* thenCompose 方法

thenCompose 方法允许你对两个 CompletionStage 进行流水线操作，第一个操作完成时，将其结果作为参数传递给第二个操作。

```java
public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn);
public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) ;
public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage
```
    *
    * */
}
