package com.atguigu.gulimall.order.service.impl;

import com.atguigu.gulimall.common.to.mq.SeckillOrderTo;
import com.atguigu.gulimall.common.to.mq.SkuHasStockVo;
import com.atguigu.gulimall.common.vo.MemberResponseVo;
import com.atguigu.gulimall.order.constant.OrderConstant;
import com.atguigu.gulimall.order.entity.OrderItemEntity;
import com.atguigu.gulimall.order.enume.OrderStatusEnum;
import com.atguigu.gulimall.order.feign.CartFeignService;
import com.atguigu.gulimall.order.feign.MemberFeignService;
import com.atguigu.gulimall.order.feign.ProductFeignService;
import com.atguigu.gulimall.order.feign.WareFeignService;
import com.atguigu.gulimall.order.interceptor.LoginInterceptor;
import com.atguigu.gulimall.order.service.OrderItemService;
import com.atguigu.gulimall.order.service.PaymentInfoService;
import com.atguigu.gulimall.order.to.OrderCreateTo;
import com.atguigu.gulimall.order.vo.*;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.common.utils.PageUtils;
import com.atguigu.gulimall.common.utils.Query;

import com.atguigu.gulimall.order.dao.OrderDao;
import com.atguigu.gulimall.order.entity.OrderEntity;
import com.atguigu.gulimall.order.service.OrderService;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    @Autowired
    private CartFeignService cartFeignService;
    @Autowired
    private ThreadPoolExecutor executor;
    @Autowired
    private MemberFeignService memberFeignService;

    @Autowired
    private WareFeignService wareFeignService;



    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private OrderItemService orderItemService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private PaymentInfoService paymentInfoService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 订单获取数据
     * 查询购物项、库存和收货地址都要调用远程服务，串行会浪费大量时间，因此我们使用CompletableFuture进行异步编排
     * 可能由于延迟，订单提交按钮可能被点击多次，为了防止重复提交的问题，我们在返回订单确认页时，在redis中生成一个随机的令牌，
     * 过期时间为30min，提交的订单会携带这个令牌，我们将会在订单提交的处理页面核验此令牌
      * @return
     */
    @Override
    public OrderConfirmVo confirmOrder() {
        //订单详情包括  商品详情，库存情况，收货地址，优惠券服务，需要调取其他服务，使用异步编排的方式
        //可能由于延迟，订单按钮多次点击为了防止重复提交，增加了redis随机令牌，过期时间为30分钟，提交的订单会携带令牌，在订单的处理
        //接口核验令牌
        //1.1获取登录信息
        MemberResponseVo responseVo = LoginInterceptor.loginLocal.get();
        OrderConfirmVo orderConfirmVo =new OrderConfirmVo();
        //1.2从请求中获取到数据
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //异步编排查询所选中的购物车
        CompletableFuture<Void> itemAndStockFuture =CompletableFuture.supplyAsync(()->{
            RequestContextHolder.setRequestAttributes(requestAttributes);
            //2.1远程调用购物车服务查询所有选中的购物项
            List<OrderItemVo> checkedItems =cartFeignService.getCheckedItems();
            orderConfirmVo.setItems(checkedItems);
            return checkedItems;
        },executor).thenAcceptAsync(item ->{
            //2.2远程调用库存服务查询选中的购物项的库存信息
            //2.2.1取出商品id
            List<Long> skuids = item.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            //2.2.2根据id去查询每个商品的库存
            Map<Long, Boolean> hasStockMap = wareFeignService.getSkuHasStocks(skuids).stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
            orderConfirmVo.setStocks(hasStockMap);
        });
        //2.3查询所有收货地址
        CompletableFuture<Void>addressFuture =CompletableFuture.runAsync(()->{
            List<MemberAddressVo> addressByUserId = memberFeignService.getAddressByUserId(responseVo.getId());
            orderConfirmVo.setMemberAddressVos(addressByUserId);
        });
        //3从登录信息中查询积分
        orderConfirmVo.setIntegration(responseVo.getIntegration());
        //4总价在orderConfirmVo中自动计算
        //5设置防重令牌存入redis中 key为userid value为uuid，并将token存入orderConfirmVo中
        String token = UUID.randomUUID().toString().replace("-","");
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX+responseVo.getId(),token,30, TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(token);
        //确保异步编排执行完毕
        try {
            CompletableFuture.allOf(itemAndStockFuture,addressFuture).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        return orderConfirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo submitVo) {
        SubmitOrderResponseVo responseVo =new SubmitOrderResponseVo();
        //1验证防重令牌
        //为防止在获取令牌、对比值和删除令牌之间发生错误导入令牌校验出错，我们必须使用脚本保证原子性操作
        MemberResponseVo memberResponseVo=LoginInterceptor.loginLocal.get();
        String script= "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long execute = redisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberResponseVo.getId()), submitVo.getOrderToken());
        if (execute == 0L) {
            //1.1 防重令牌验证失败
            responseVo.setCode(1);
            return responseVo;
        }else {
            //2创建订单、订单项
            OrderCreateTo order =createOrderTo(memberResponseVo,submitVo);

        }

        //3验价格
        //4保存订单
        //5锁定库存
        return null;
    }

    /**
     * 创建订单
     * @param memberResponseVo
     * @param submitVo
     * @return
     */
    private OrderCreateTo createOrderTo(MemberResponseVo memberResponseVo, OrderSubmitVo submitVo) {
        //用IdWorker生成订单号
        String orderSn = IdWorker.getTimeId();
        //创建订单
        OrderEntity entity = buildOrder(memberResponseVo, submitVo,orderSn);
        //构建订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItems(orderSn);
        //计算价格
        return null;
    }

    /**
     * 创建订单项
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItems(String orderSn) {
        //1.远程调用购物车服务查询选中的商品
        List<OrderItemVo> checkedItems = cartFeignService.getCheckedItems();
        //循环遍历商品设置商品属性
        List<OrderItemEntity> orderItemEntities = checkedItems.stream().map((item) -> {
            OrderItemEntity orderItemEntity = buildOrderItem(item);
            orderItemEntity.setOrderSn(orderSn);
            return orderItemEntity;
        }).collect(Collectors.toList());
        return orderItemEntities;
    }

    /**
     * 创建单个订单项
     * @param item
     * @return
     */
    private OrderItemEntity buildOrderItem(OrderItemVo item) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        //1获取订单id
        Long skuId = item.getSkuId();
        //2设置sku相关属性
        orderItemEntity.setSkuId(skuId);
        orderItemEntity.setSkuName(item.getTitle());
        orderItemEntity.setSkuAttrsVals(StringUtils.collectionToDelimitedString(item.getSkuAttrValues(), ";"));
        orderItemEntity.setSkuPic(item.getImage());
        orderItemEntity.setSkuPrice(item.getPrice());
        orderItemEntity.setSkuQuantity(item.getCount());
        //3通过skuId查询spu相关属性并设置
        //4商品的优惠信息不做
        //5商品的积分成长，为价格x数量
        //6订单项订单价格信息
        //7实际价格
        return orderItemEntity;
    }

    /**
     * 创建订单
     * @param memberResponseVo
     * @param submitVo
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(MemberResponseVo memberResponseVo, OrderSubmitVo submitVo, String orderSn) {
        OrderEntity orderEntity =new OrderEntity();
        //1获取到订单id
        orderEntity.setOrderSn(orderSn);
        //2从请求中获取并设置用户信息
        orderEntity.setMemberId(memberResponseVo.getId());
        orderEntity.setMemberUsername(memberResponseVo.getUsername());
        //3远程服务获取邮费和收件人信息并设置
        FareVo fareVo = wareFeignService.getFare(submitVo.getAddrId());
        BigDecimal fare = fareVo.getFare();
        orderEntity.setFreightAmount(fare);
        MemberAddressVo address = fareVo.getAddress();
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        //4设置订单相关的状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());//代付款状态
        orderEntity.setConfirmStatus(0);//确认收货状态[0->未确认；1->已确认]
        orderEntity.setAutoConfirmDay(7);//自动确认时间
        return orderEntity;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        return null;
    }

    @Override
    public void closeOrder(OrderEntity orderEntity) {

    }

    @Override
    public PageUtils getMemberOrderPage(Map<String, Object> params) {
        return null;
    }

    @Override
    public PayVo getOrderPay(String orderSn) {
        return null;
    }

    @Override
    public void handlerPayResult(PayAsyncVo payAsyncVo) {

    }

    @Override
    public void createSeckillOrder(SeckillOrderTo orderTo) {

    }

    public static void main(String[] args) {
        List<String> list =Arrays.asList("张三","李四","王五","李恩");
        String aa =StringUtils.collectionToDelimitedString(list,",");
        System.out.println(aa);
    }
}
