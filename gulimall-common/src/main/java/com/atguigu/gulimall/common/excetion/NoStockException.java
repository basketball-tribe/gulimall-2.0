package com.atguigu.gulimall.common.excetion;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName NoStockException
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/4
 * @Version V1.0
 **/
public class NoStockException extends RuntimeException{
    @Setter
    @Getter
    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品id:"+skuId+";库存不足");
    }

    public NoStockException(String message) {
        super(message);
    }
}
