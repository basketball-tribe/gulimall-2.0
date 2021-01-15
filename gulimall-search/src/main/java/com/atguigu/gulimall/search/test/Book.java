package com.atguigu.gulimall.search.test;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * @ClassName Boot
 * @Description: TODO
 * @Author fengjc
 * @Date 2021/1/15
 * @Version V1.0
 **/
@Data
@NoArgsConstructor
@Accessors(chain = true)
@Document(indexName = "book",type = "book", shards = 1, replicas = 0)
public class Book  {
    @Id
    private Integer id;
    @Field(type = FieldType.Keyword)
    private String bookName;
    @Field(type = FieldType.Keyword)
    private String author;
}
