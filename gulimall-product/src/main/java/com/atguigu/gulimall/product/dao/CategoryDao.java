package com.atguigu.gulimall.product.dao;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-11 17:33:50
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
