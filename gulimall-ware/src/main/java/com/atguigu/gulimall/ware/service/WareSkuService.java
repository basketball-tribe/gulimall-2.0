package com.atguigu.gulimall.ware.service;

import com.atguigu.gulimall.common.to.mq.SkuHasStockVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.common.utils.PageUtils;
import com.atguigu.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-10 18:40:41
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 根据商品id查询库存信息
     * @param ids
     * @return
     */
    List<SkuHasStockVo> getSkuHasStocks(List<Long> ids);
}

