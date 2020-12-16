package com.atguigu.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-11 17:33:50
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 以tree形式查询商品
     * @return
     */
    List<CategoryEntity> listWithTree();

    /**
     * 逻辑删除
     * @param asList
     */
    void removeMenuByIds(List<Long> asList);

    /**
     *找到该三级分类的完整路径
     * @param catelogId
     * @return
     */
    Long[] findCatelogPathById(Long catelogId);
}

