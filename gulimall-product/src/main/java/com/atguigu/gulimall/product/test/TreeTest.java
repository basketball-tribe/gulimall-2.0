package com.atguigu.gulimall.product.test;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName TreeTest
 * @Description: 递归查询Tree信息
 * @Author fengjc
 * @Date 2021/1/22
 * @Version V1.0
 **/
public class TreeTest {

    public static void main(String[] args) {
        List<TestEntity> list = getTree();
    }

    private static List<TestEntity> getTree() {
        //1.从数据库中查询出所有的数据 //假设已经从数据库中一次性查到了所有的值
        List<TestEntity> testEntities = new ArrayList<>();
        //2.首先查出父级分类
        List<TestEntity> list = testEntities.stream().filter(testEntity -> testEntity.getParentId() == 0)
                .map(testEntity -> {
                    //3.将本次查询结果和全部的结果当成参数传入方法中查询子集信息
                    testEntity.setChildren(getChildren(testEntity, testEntities));
                    return testEntity;
                }).sorted(Comparator.comparingInt(item -> item.getSortId() == null ? 0 : item.getSortId())).collect(Collectors.toList());
        return list;
    }

    /**
     * 递归插叙子集信息
     * @param root
     * @param all
     * @return
     */
    private static List<TestEntity> getChildren(TestEntity root, List<TestEntity> all) {
        List<TestEntity> testEntityList = all.stream().filter(testEntity -> testEntity.getParentId().equals(root.getId()))
                .map(testEntity -> {
                    testEntity.setChildren(getChildren(root, all));
                    return testEntity;
                }).sorted(Comparator.comparingInt(item -> item.getSortId() == null ? 0 : item.getSortId())).collect(Collectors.toList());
        return testEntityList;
    }
}
