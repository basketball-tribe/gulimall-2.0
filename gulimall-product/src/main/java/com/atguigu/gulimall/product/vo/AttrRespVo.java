package com.atguigu.gulimall.product.vo;

import lombok.Data;

/**
 * @ClassName AttrRespVo
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/16
 * @Version V1.0
 **/
@Data
public class AttrRespVo extends AttrVo{
    /**
     * 			"catelogName": "手机/数码/手机", //所属分类名字
     * 			"groupName": "主体", //所属分组名字
     */
    private String catelogName;
    private String groupName;

    private Long[] catelogPath;
}
