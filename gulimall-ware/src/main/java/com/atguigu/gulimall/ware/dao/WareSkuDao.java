package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 *
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-10 18:40:41
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
    /**
     * 根据id查询商品库存数量
     * @param id
     * @return
     */
    Integer getTotalStock(@Param("id") Long id);

    /**
     * 找出所有库存大于商品数的仓库
     * @param skuId
     * @param count
     * @return
     */
    List<Long> listWareIdsHasStock(Long skuId, Integer count);

    /**
     * 锁定库存信息
     * @param skuId
     * @param num
     * @param wareId
     * @return
     */
    Long lockWareSku(Long skuId, Integer num, Long wareId);

    /**
     * 解锁库存信息
     * @param skuId
     * @param skuNum
     * @param wareId
     */
    void unlockStock(Long skuId, Integer skuNum, Long wareId);
}
