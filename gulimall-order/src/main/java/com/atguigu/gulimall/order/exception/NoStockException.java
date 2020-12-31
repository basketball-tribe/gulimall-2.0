package com.atguigu.gulimall.order.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName NoStockException
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/31
 * @Version V1.0
 **/
public class NoStockException extends RuntimeException{
    @Setter
    @Getter
    private Long skuId;

    public NoStockException(Long skuId) {
       super("商品id"+skuId+";库存不足");
    }

    public NoStockException(String message) {
        super(message);
    }
}
