package com.atguigu.gulimall.ware.dao;

import com.atguigu.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-10 18:40:41
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
