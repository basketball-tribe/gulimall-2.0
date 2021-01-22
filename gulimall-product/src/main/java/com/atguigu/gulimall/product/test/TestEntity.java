package com.atguigu.gulimall.product.test;

import lombok.Data;

import java.util.List;

/**
 * @ClassName TestEntity
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/22
 * @Version V1.0
 **/
@Data
public class TestEntity {
    private Integer id;
    private Integer parentId;
    private Integer sortId;
    private String name;
    private List<TestEntity> children;
}
