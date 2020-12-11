package com.atguigu.gulimall.coupon.dao;

import com.atguigu.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-11 17:38:45
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
