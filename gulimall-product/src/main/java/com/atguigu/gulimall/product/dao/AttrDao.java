package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品属性
 * 
 * @author fengjc
 * @email gulimall@qq.com
 * @date 2020-12-10 13:13:40
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {
	
}
