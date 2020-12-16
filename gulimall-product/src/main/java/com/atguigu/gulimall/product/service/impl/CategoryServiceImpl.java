package com.atguigu.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.common.utils.PageUtils;
import com.atguigu.gulimall.common.utils.Query;

import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 以tree形式查询商品
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        //1.查询出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //递归查询子分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0)
                .map(categoryEntity -> {
                    //将总数和本次要查询的数据(主要是为了获取到本类的id作为子类的父级id)存入需要递归的方法中
                    categoryEntity.setChildren(getChildrens(categoryEntity,entities));
                    return categoryEntity;
                })
                .sorted(
                (meau1, meau2) -> {
                    return (meau1.getSort() == null ? 0 : meau1.getSort()) - (meau2.getSort() == null ? 0 : meau2.getSort());
                }
        )
                .collect(Collectors.toList());
        return level1Menus;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        //TODO  1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPathById(Long categorygId) {
         List<Long> path =new ArrayList<>();
        findPath(categorygId, path);
        Collections.reverse(path);
        Long[] objects = path.toArray(new Long[path.size()]);
        return objects;
    }
    private void findPath(Long categorygId, List<Long> path){
        if(categorygId !=0){
            path.add(categorygId);
            CategoryEntity categoryEntity =getById(categorygId);
            findPath(categoryEntity.getParentCid(),path);
        }
    }

    //递归查询所有的子类菜单
    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        List<CategoryEntity> children = all.stream().filter(
                //父类的catId作为子类的parentCid
                categoryEntity -> categoryEntity.getParentCid() == root.getCatId()
        ).map(categoryEntity -> {
            //将总数和本次要查询的数据(主要是为了获取到本类的id作为子类的父级id)存入需要递归的方法中
            categoryEntity.setChildren(getChildrens(categoryEntity, all));
            return categoryEntity;
        }).sorted(
                (meau1, meau2) -> {
                    return (meau1.getSort() == null ? 0 : meau1.getSort()) - (meau2.getSort() == null ? 0 : meau2.getSort());
                }
        ).collect(Collectors.toList());
        return children;
    }

}
