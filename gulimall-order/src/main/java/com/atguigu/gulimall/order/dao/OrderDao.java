package com.atguigu.gulimall.order.dao;

import com.atguigu.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 *
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-10 18:34:05
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

}
