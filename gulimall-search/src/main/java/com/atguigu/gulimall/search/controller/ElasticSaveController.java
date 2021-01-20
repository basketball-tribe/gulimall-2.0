package com.atguigu.gulimall.search.controller;

import com.atguigu.gulimall.common.excetion.BizCodeEnume;
import com.atguigu.gulimall.common.to.es.SkuEsModel;
import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName ElasticSaveController
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/18
 * @Version V1.0
 **/
@Slf4j
@RestController
public class ElasticSaveController {
    @Autowired
    private ProductSaveService productSaveService;
    @PostMapping("/product")
    public R saveProductAsIndices(@RequestBody List<SkuEsModel> esModels){
        boolean flag =false;
        try {
            flag = productSaveService.productStatusUp(esModels);
        }catch (Exception e){
            log.error("ElasticSaveController商品上架产生了错误：{}", e);
            return R.error(BizCodeEnume.PRODUCT_UP_TO_ES_EXCETION.getCode(), BizCodeEnume.PRODUCT_UP_TO_ES_EXCETION.getMsg());
        }
        if (!flag) {
            return R.ok();
        } else {
            return R.error(BizCodeEnume.PRODUCT_UP_TO_ES_EXCETION.getCode(), BizCodeEnume.PRODUCT_UP_TO_ES_EXCETION.getMsg());
        }
    }
}
