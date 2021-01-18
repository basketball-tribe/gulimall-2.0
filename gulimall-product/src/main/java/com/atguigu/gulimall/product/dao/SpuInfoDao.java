package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * spu信息
 * 
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-11 17:33:50
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void upSpuStatus(Long spuId, int code);
}
