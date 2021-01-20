package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.product.vo.Catalog2Vo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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
    @Autowired
    StringRedisTemplate redisTemplate;
    private Map<String, Object> cache = new HashMap<>();

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
     *
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        return listWithTreeRedisLock2();
    }

    //分布式锁
    private List<CategoryEntity> listWithTreeRedisLock2() {
        String uuid = UUID.randomUUID().toString();
        //使用setIfAbsent 来确保插入值和插入过期时间为原子性
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 5, TimeUnit.SECONDS);
        if (lock) {
            List<CategoryEntity> categoryEntitiesResult = listWithTreeRedisLock1();
            String lockValue = redisTemplate.opsForValue().get("lock");
            String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
                    "    return redis.call(\"del\",KEYS[1])\n" +
                    "else\n" +
                    "    return 0\n" +
                    "end";
            // 删除锁必须保证原子性。使用redis+Lua脚本完成
            redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), lockValue);
            return categoryEntitiesResult;
        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return listWithTreeRedisLock2();
        }

    }

    //单机版不需要考虑分布式
    private List<CategoryEntity> listWithTreeRedisLock1() {
        List<CategoryEntity> categoryEntitiesResult1 = new ArrayList<>();
        String categoryEntities1 = redisTemplate.opsForValue().get("categoryEntities");
        if (StringUtils.isEmpty(categoryEntities1)) {
            System.out.println("缓存没有，从DB中查询");
            synchronized (this) {
                String categoryEntities2 = redisTemplate.opsForValue().get("categoryEntities");
                if (StringUtils.isEmpty(categoryEntities2)) {
                    List<CategoryEntity> categoryEntitiesResult2 = listWithTreeFromDB2();
                    redisTemplate.opsForValue().set("categoryEntities", JSON.toJSONString(categoryEntitiesResult2));
                    return categoryEntitiesResult2;
                } else {
                    List<CategoryEntity> categoryEntitiesResult3 = JSON.parseObject(categoryEntities2, new TypeReference<List<CategoryEntity>>() {
                    });
                    return categoryEntitiesResult3;
                }
            }

        }
        categoryEntitiesResult1 = JSON.parseObject(categoryEntities1, new TypeReference<List<CategoryEntity>>() {
        });
        return categoryEntitiesResult1;
    }


    /**
     * 不加锁版
     *
     * @return
     */
    private List<CategoryEntity> listWithTreeFromDB1() {
        List<CategoryEntity> categoryEntities = new ArrayList<>();
        //测试map缓存，只是测试map缓存，正式不在使用map缓存
        categoryEntities = (List<CategoryEntity>) cache.get("categoryEntities");
        if (categoryEntities == null || categoryEntities.size() <= 0) {
            //1.查询出所有分类
            List<CategoryEntity> entities = baseMapper.selectList(null);
            //递归查询子分类
            List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                    categoryEntity.getParentCid() == 0)
                    .map(categoryEntity -> {
                        //将总数和本次要查询的数据(主要是为了获取到本类的id作为子类的父级id)存入需要递归的方法中
                        categoryEntity.setChildren(getChildrens(categoryEntity, entities));
                        return categoryEntity;
                    })
                    .sorted(
                            (meau1, meau2) -> {
                                return (meau1.getSort() == null ? 0 : meau1.getSort()) - (meau2.getSort() == null ? 0 : meau2.getSort());
                            }
                    )
                    .collect(Collectors.toList());
            cache.put("categoryEntities", level1Menus);
            return level1Menus;
        }
        return categoryEntities;
    }

    /*
     * 加锁版，插入数据库加锁可以防止多次缓存击穿
     * */
    private synchronized List<CategoryEntity> listWithTreeFromDB2() {
        //1.查询出所有分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        //递归查询子分类
        List<CategoryEntity> level1Menus = entities.stream().filter(categoryEntity ->
                categoryEntity.getParentCid() == 0)
                .map(categoryEntity -> {
                    //将总数和本次要查询的数据(主要是为了获取到本类的id作为子类的父级id)存入需要递归的方法中
                    categoryEntity.setChildren(getChildrens(categoryEntity, entities));
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
        List<Long> path = new ArrayList<>();
        findPath(categorygId, path);
        Collections.reverse(path);
        Long[] objects = path.toArray(new Long[path.size()]);
        return objects;
    }

    @Override
    public List<CategoryEntity> getLevel1Catagories() {
//        long start = System.currentTimeMillis();
        List<CategoryEntity> parent_cid = this.list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
//        System.out.println("查询一级菜单时间:"+(System.currentTimeMillis()-start));
        return parent_cid;
    }

    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJsonDbWithSpringCache() {
        return getCategoriesDb();
    }

    //从数据库中查出三级分类
    private Map<String, List<Catalog2Vo>> getCategoriesDb() {
        System.out.println("查询了数据库");
        //优化业务逻辑，仅查询一次数据库
        List<CategoryEntity> categoryEntities = this.list();
        //查出所有一级分类
        List<CategoryEntity> level1Categories = getCategoryByParentCid(categoryEntities, 0L);
        Map<String, List<Catalog2Vo>> listMap = level1Categories.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
            //遍历查找出二级分类
            List<CategoryEntity> level2Categories = getCategoryByParentCid(categoryEntities, v.getCatId());
            List<Catalog2Vo> catalog2Vos = null;
            if (level2Categories != null) {
                //封装二级分类到vo并且查出其中的三级分类
                catalog2Vos = level2Categories.stream().map(cat -> {
                    //遍历查出三级分类并封装
                    List<CategoryEntity> level3Catagories = getCategoryByParentCid(categoryEntities, cat.getCatId());
                    List<Catalog2Vo.Catalog3Vo> catalog3Vos = null;
                    if (level3Catagories != null) {
                        catalog3Vos = level3Catagories.stream()
                                .map(level3 -> new Catalog2Vo.Catalog3Vo(level3.getParentCid().toString(), level3.getCatId().toString(), level3.getName()))
                                .collect(Collectors.toList());
                    }
                    Catalog2Vo catalog2Vo = new Catalog2Vo(v.getCatId().toString(), cat.getCatId().toString(), cat.getName(), catalog3Vos);
                    return catalog2Vo;
                }).collect(Collectors.toList());
            }
            return catalog2Vos;
        }));
        return listMap;
    }

    private List<CategoryEntity> getCategoryByParentCid(List<CategoryEntity> categoryEntities, long l) {
        List<CategoryEntity> collect = categoryEntities.stream().filter(cat -> cat.getParentCid() == l).collect(Collectors.toList());
        return collect;
    }

    private void findPath(Long categorygId, List<Long> path) {
        if (categorygId != 0) {
            path.add(categorygId);
            CategoryEntity categoryEntity = getById(categorygId);
            findPath(categoryEntity.getParentCid(), path);
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
