package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {
    boolean saveProductAsIndices(List<SkuEsModel> skuEsModels) throws IOException;
}
