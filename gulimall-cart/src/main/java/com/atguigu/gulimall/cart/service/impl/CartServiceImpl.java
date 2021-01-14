package com.atguigu.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.cart.config.CartConstant;
import com.atguigu.gulimall.cart.feign.ProductFeignService;
import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import com.atguigu.gulimall.cart.service.CartService;
import com.atguigu.gulimall.cart.to.UserInfoTo;
import com.atguigu.gulimall.cart.vo.CartItemVo;
import com.atguigu.gulimall.cart.vo.CartVo;
import com.atguigu.gulimall.cart.vo.SkuInfoVo;
import com.atguigu.gulimall.common.utils.R;
import jdk.nashorn.internal.parser.JSONParser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @ClassName CartServiceImpl
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/30
 * @Version V1.0
 **/
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private ThreadPoolExecutor executor;

    /**
     * 加入购物车
     * 1.判断当前用户的用户标识(是否登录，登录了使用userid，未登录使用在cookie中存储的user_key)
     * 2.判断本次添加的商品是否存在购物车中，若存在则加数量
     * 3.若不存在则添加，远程调用商品服务查询商品的信息，通过异步查询，将信息存入redis中
     * 4.以后使用则从redis中获取购物车信息
     *
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItemVo addCartItem(Long skuId, Integer num) {
        //获取当前以当前用户标识为key的hash的操作
        BoundHashOperations<String, Object, Object> ops = getCartItemOps();
        //判断此商品是否存在在购物车中
        String cartJson = (String) ops.get(skuId.toString());
        //已经存在在购物车中，则取出数据数量加1
        if (!StringUtils.isEmpty(cartJson)) {
            //1.1将数据取出并count+
            CartItemVo cartItemVo = JSON.parseObject(cartJson, CartItemVo.class);
            cartItemVo.setCount(cartItemVo.getCount() + num);
            //1.2将更新后的对象转为Json并存入redis中
            String json = JSON.toJSONString(cartItemVo);
            ops.put(skuId.toString(), json);
            return cartItemVo;
        } else {
            //没有存在购物车中，则新增
            CartItemVo cartItemVo = new CartItemVo();
            //2.1异步远程调用商品服务查询商品信息缓存到redis中
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                //2.1 远程查询sku基本信息
                R info = productFeignService.info(skuId);
                SkuInfoVo skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItemVo.setCheck(true);
                cartItemVo.setCount(num);
                cartItemVo.setImage(skuInfo.getSkuDefaultImg());
                cartItemVo.setPrice(skuInfo.getPrice());
                cartItemVo.setSkuId(skuId);
                cartItemVo.setTitle(skuInfo.getSkuTitle());
            }, executor);
            //2.2 远程查询sku属性组合信息
            CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                List<String> attrValuesAsString = productFeignService.getSkuSaleAttrValuesAsString(skuId);
                cartItemVo.setSkuAttrValues(attrValuesAsString);
            }, executor);
            try {
                CompletableFuture.allOf(future1, future1).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            //2.3 将该属性封装并存入redis,登录用户使用userId为key,否则使用user-key
            String toJson = JSON.toJSONString(cartItemVo);
            ops.put(skuId.toString(), toJson);
            return cartItemVo;
        }

    }

    @Override
    public CartItemVo getCartItem(Long skuId) {

        return null;
    }
    /**
     * 获取购物车信息
     * 若用户未登录，则直接使用user-key获取购物车数据
     * 否则使用userId获取购物车数据，并将user-key对应临时购物车数据与用户购物车数据合并，并删除临时购物车
     * @return
     */
    @Override
    public CartVo getCart() {
        CartVo cartVo = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        //先使用user_key查询redis中缓存的商品信息
        List<CartItemVo> tempCart = getCartByKey(userInfoTo.getUserKey());
        if(StringUtils.isEmpty(userInfoTo.getUserId())){
            //1 用户未登录，直接通过user-key获取临时购物车
            cartVo.setItems(tempCart);
        }else {
            //2 用户已登录
            //2.1使用userId查询userId对应的购物车
            List<CartItemVo> userCart = getCartByKey(userInfoTo.getUserId().toString());
            //2.2 查询user-key对应的临时购物车，并和用户购物车合并
            if(tempCart !=null ||tempCart.size()>0){
                BoundHashOperations<String, Object, Object> ops =redisTemplate.boundHashOps(CartConstant.CART_PREFIX+userInfoTo.getUserId());
                for(CartItemVo cartItemVo:tempCart){
                    userCart.add(cartItemVo);
                    //2.3 在redis中更新数据
                    //此时的用户为已经登录的更新的是redis中存储userId的数据
                    addCartItem(cartItemVo.getSkuId(), cartItemVo.getCount());
                }

            }
            cartVo.setItems(userCart);
            //2.4 更新完毕之后删除之前临时存储的数据(用户未登录存储的数据)
            redisTemplate.delete(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
        }
        return cartVo;
    }


    /**
     * 修改购物车选中情况
     * @param skuId
     * @param isChecked
     */
    @Override
    public void checkCart(Long skuId, Integer isChecked) {
        BoundHashOperations<String, Object, Object> ops = getCartItemOps();
        String cartJson= (String) ops.get(skuId.toString());
        //查询出缓存中商品信息
        CartItemVo cartItemVo =JSONObject.parseObject(cartJson,CartItemVo.class);
        //修改商品信息并更新进缓存中
        cartItemVo.setCheck(isChecked==1);
        ops.put(skuId.toString(),JSON.toJSONString(cartItemVo));
    }

    /**
     * 修改购物车中的商品数量
     * @param skuId
     * @param num
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> ops = getCartItemOps();
        String cartJson= (String) ops.get(skuId.toString());
        //查询出缓存中商品信息
        CartItemVo cartItemVo =JSONObject.parseObject(cartJson,CartItemVo.class);
        //修改商品信息并更新进缓存中
        cartItemVo.setCount(num);
        ops.put(skuId.toString(),JSON.toJSONString(cartItemVo));
    }

    /**
     * 删除购物车商品
     * @param skuId
     */
    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartItemOps();
        ops.delete(skuId.toString());
    }

    /**
     * 清空购物车
     */
    @Override
    public void deleteItemAll(){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if(StringUtils.isEmpty(userInfoTo.getUserId())){
            redisTemplate.delete(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
        }else {
            redisTemplate.delete(CartConstant.CART_PREFIX + userInfoTo.getUserId());
        }

    }
    /**
     * 选中的购物车信息
     * @return
     */
    @Override
    public List<CartItemVo> getCheckedItems() {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        List<CartItemVo> cartByKey = getCartByKey(userInfoTo.getUserId().toString());
        return cartByKey.stream().filter(CartItemVo::getCheck).collect(Collectors.toList());
    }

    /**
     * 获取用户是否登录
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartItemOps() {
        //1判断是否已经登录
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (!StringUtils.isEmpty(userInfoTo.getUserId())) {
            //1.1 登录使用userId操作redis
            return redisTemplate.boundHashOps(CartConstant.CART_PREFIX + userInfoTo.getUserId());
        } else {
            //1.2 未登录使用user-key操作redis
            return redisTemplate.boundHashOps(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
        }

    }

    /**
     * 用户未登录通过userKey获取购物车信息
     * @param userKey
     * @return
     */
    private List<CartItemVo> getCartByKey(String userKey) {
        //获取缓存中的商品信息
        BoundHashOperations<String, Object, Object> ops =redisTemplate.boundHashOps(CartConstant.CART_PREFIX+userKey);
        List<Object> values =ops.values();
        if(!values.isEmpty()){
            List<CartItemVo> cartItemVoList = values.stream().map(object -> {
                String json = (String) object;
                return JSON.parseObject(json, CartItemVo.class);
            }).collect(Collectors.toList());
            return cartItemVoList;
        }
        return null;
    }
}
