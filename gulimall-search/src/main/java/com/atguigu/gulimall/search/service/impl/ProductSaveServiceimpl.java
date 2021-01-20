package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean saveProductAsIndices(List<SkuEsModel> skuEsModels) throws IOException {


        return false;
    }
    /**
     * 上传到es
     *
     * @param esModels
     * @return true 有错误 false 无错误
     * @throws IOException
     */
    @Override
    public boolean productStatusUp(List<SkuEsModel> esModels) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for(SkuEsModel esModel :esModels){
            IndexRequest indexRequest =new IndexRequest(EsConstant.PRODUCT_INDEX);
                indexRequest.id(esModel.getSkuId().toString());
                String json = JSON.toJSONString(esModel);
                indexRequest.source(json, XContentType.JSON);
                bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        //TODO 处理产生的错误
        //这是一个是否成功的返回结果 true有错误信息  false无错误信息
        boolean hasFaile = bulk.hasFailures();
        List<String> collect = Arrays.asList(bulk.getItems()).stream().map(item -> {
            return item.getId();
        }).collect(Collectors.toList());
        log.info("ProductSaveServiceImpl.productStstusUp 上架成功商品：{} ,返回的数据：{}", collect, bulk.toString());
        return hasFaile;
    }
}
