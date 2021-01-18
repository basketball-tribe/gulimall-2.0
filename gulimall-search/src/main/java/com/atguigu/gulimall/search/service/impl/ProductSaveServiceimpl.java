package com.atguigu.gulimall.search.service.impl;

import com.atguigu.gulimall.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName ProductSaveServiceimpl
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/18
 * @Version V1.0
 **/
@Service("ProductSaveService")
@Slf4j
public class ProductSaveServiceimpl implements ProductSaveService {

    @Override
    public boolean saveProductAsIndices(List<SkuEsModel> skuEsModels) throws IOException {


        return false;
    }
}
