package com.atguigu.gulimall.product.service.impl;

import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.vo.AttrRespVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.common.utils.PageUtils;
import com.atguigu.gulimall.common.utils.Query;

import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.service.AttrService;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;
    @Autowired
    AttrGroupDao attrGroupDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params, long catelogId, String attrType) {
        //根据attrType进行查询，1规格参数，2销售属性
        QueryWrapper<AttrEntity> attrEntityQueryWrapper = new
                QueryWrapper<AttrEntity>().eq("attr_type", "base".equalsIgnoreCase(attrType) ? 1 : 0);
        //如果参数带有分类id，则按分类查询
        if (catelogId != 0) {
            attrEntityQueryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        //搜索的模糊查询
        if (!StringUtils.isEmpty(key)) {
            attrEntityQueryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
//            wrapper.eq("attr_id",key).or().likeLeft("attr_name",key);//左like
//            wrapper.eq("attr_id",key).or().likeRight("attr_name",key);//右like
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), attrEntityQueryWrapper);
        List<AttrEntity> records = page.getRecords();
        List<AttrRespVo> collect = records.stream().map((entity) -> {
                    AttrRespVo respVo = new AttrRespVo();
                    BeanUtils.copyProperties(entity, respVo);
                    //查询分类并设置分类名
                    CategoryEntity categoryEntity = categoryDao.selectOne(new QueryWrapper<CategoryEntity>().eq("cat_id", entity.getCatelogId()));
                    //如果是查询规格参数才查询设置分组名
                    if ("base".equalsIgnoreCase(attrType)) {
                        //查询参数、分组关系
                        AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", entity.getAttrId()));
                        //如果分组id不为空。则查出分组名
                        if (attrAttrgroupRelationEntity != null && attrAttrgroupRelationEntity.getAttrGroupId() != null) {
                            AttrGroupEntity attrGroupEntity = attrGroupDao.selectOne(new QueryWrapper<AttrGroupEntity>().eq("attr_group_id", attrAttrgroupRelationEntity.getAttrGroupId()));
                            //设置分组名
                            respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                        }
                    }
                    return respVo;
                }
        ).collect(Collectors.toList());
        PageUtils pageUtils =new PageUtils(page);
        pageUtils.setList(collect);
        return pageUtils;
    }

    @Override
    public void saveAttr(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);
    }

    @Override
    public List<Long> selectSearchAttrIds(List<Long> attrIds) {
        return baseMapper.selectSearchAttrIds(attrIds);
    }

}
