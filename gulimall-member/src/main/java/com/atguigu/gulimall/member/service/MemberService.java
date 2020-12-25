package com.atguigu.gulimall.member.service;

import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import com.atguigu.gulimall.member.vo.SocialUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gulimall.common.utils.PageUtils;
import com.atguigu.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author fengjc
 * @email fengjc@mail.com
 * @date 2020-12-10 18:42:17
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 注册会员
     * @param registerVo
     */
    void register(MemberRegisterVo registerVo);

    /**
     *第三方微博登录
     * @param socialUser
     * @return
     */
    MemberEntity login(SocialUser socialUser);
}

