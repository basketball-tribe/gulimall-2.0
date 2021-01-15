package com.atguigu.gulimall.search.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * @ClassName ElasticSearchTest
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/15
 * @Version V1.0
 **/
@RestController
public class ElasticSearchTest {
    @Autowired
    private ElasticRepository elasticRepository;
    @Autowired
    private ElasticsearchRepository elasticsearchRepository;
    @GetMapping("/save")
    public String save(){
        DocBean docBean =new DocBean();
        docBean.setId(0L);
        docBean.setFirstCode("first");
        docBean.setSecordCode("sec");
        docBean.setContent("asdadadadasdaewadadsadwsadas");
        docBean.setType(0);
        elasticRepository.save(docBean);
        return "success";
    }
    @GetMapping("/get/{id}")
    public String get(@PathVariable("id") Long id){
        Optional<DocBean> byId = elasticRepository.findById(id);
        DocBean book = byId.get();
        return book.toString();
    }
    @GetMapping("save1")
    public String save1(){
        Book book=new Book();
        book.setId(10);
        book.setBookName("book的");
        book.setAuthor("book");

       elasticsearchRepository.save(book);
        return "success";
    }
    @GetMapping("/get1/{id}")
    public String get1(@PathVariable("id") Long id){
        Optional<Book> byId = elasticsearchRepository.findById(id);
        Book book = byId.get();
        return book.toString();
    }
}
