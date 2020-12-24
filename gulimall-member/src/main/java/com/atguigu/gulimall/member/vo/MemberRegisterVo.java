package com.atguigu.gulimall.member.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName MemberRegisterVo
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/24
 * @Version V1.0
 **/
@Data
public class MemberRegisterVo implements Serializable {
    private String userName;

    private String password;

    private String phone;
}
