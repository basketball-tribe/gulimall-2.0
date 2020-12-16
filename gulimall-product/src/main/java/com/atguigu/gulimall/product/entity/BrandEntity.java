package com.atguigu.gulimall.product.entity;

import com.atguigu.gulimall.common.group.AddGroup;
import com.atguigu.gulimall.common.group.UpdateGroup;
import com.atguigu.gulimall.common.listvalue.ListValue;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-11 17:33:50
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 品牌id
	 */
	@TableId
	@Null(message = "添加时品牌id必须为空",groups ={AddGroup.class} )
	@NotNull(message = "修改时品牌id不能为空",groups = {UpdateGroup.class})
	private Long brandId;
	/**
	 * 品牌名
	 */
	@NotEmpty(message = "品牌名必须提交",groups = {AddGroup.class,UpdateGroup.class})
	private String name;
	/**
	 * 品牌logo地址
	 */

	@URL(message = "提交品牌logo的地址不是有效网址",groups = {AddGroup.class,UpdateGroup.class})
	private String logo;
	/**
	 * 介绍
	 */

	private String descript;
	/**
	 * 显示状态[0-不显示；1-显示]
	 */
	@NotNull(message = "显示状态不能为空",groups = {AddGroup.class,UpdateGroup.class})
//	@ListValue(value = {0,1},groups ={AddGroup.class})
	private Integer showStatus;
	/**
	 * 检索首字母
	 */
	@NotEmpty(message = "检索首字母不能为空",groups = {AddGroup.class,UpdateGroup.class})
	@Pattern(regexp = "^[a-zA-Z]$",message = "检索字符必须是一个字母",groups = {AddGroup.class,UpdateGroup.class})
	private String firstLetter;
	/**
	 * 排序
	 */
	@NotNull(groups = {AddGroup.class, UpdateGroup.class})
	private Integer sort;

}
