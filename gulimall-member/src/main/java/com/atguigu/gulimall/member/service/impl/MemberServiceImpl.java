package com.atguigu.gulimall.member.service.impl;

import com.atguigu.gulimall.member.entity.MemberLevelEntity;
import com.atguigu.gulimall.member.exception.PhoneNumExistException;
import com.atguigu.gulimall.member.exception.UserExistException;
import com.atguigu.gulimall.member.service.MemberLevelService;
import com.atguigu.gulimall.member.vo.MemberRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gulimall.common.utils.PageUtils;
import com.atguigu.gulimall.common.utils.Query;

import com.atguigu.gulimall.member.dao.MemberDao;
import com.atguigu.gulimall.member.entity.MemberEntity;
import com.atguigu.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {
    @Autowired
    private MemberLevelService memberLevelService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }
    /**
     * 注册会员
     * @param registerVo
     */
    @Override
    public void register(MemberRegisterVo registerVo) {
        //1 检查电话号是否唯一
        checkPhoneUnique(registerVo.getPhone());
        //2 检查用户名是否唯一
        checkUserNameUnique(registerVo.getUserName());
        //3 该用户信息唯一，进行插入
        MemberEntity entity =new MemberEntity();
           entity.setUsername(registerVo.getUserName());
           entity.setMobile(registerVo.getPhone());
            entity.setCreateTime(new Date());
            //使用加密保存密码
        BCryptPasswordEncoder passwordEncoder =new BCryptPasswordEncoder();
        String encodePassword = passwordEncoder.encode(registerVo.getPassword());
        entity.setPassword(encodePassword);
        //3.3 设置会员默认等级
        //3.3.1 找到会员默认登记
        MemberLevelEntity defaultLevel = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().eq("default_status", 1));
        //3.3.2 设置会员等级为默认
        entity.setLevelId(defaultLevel.getId());
        // 4 保存用户信息
        this.save(entity);
    }

    /**
     * 检验用户是否唯一
     * @param userName
     */
    private void checkUserNameUnique(String userName) {
        Integer count =baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username",userName));
        if(count >0){
            throw new UserExistException();
        }
    }

    /**
     * 检查手机号是否唯一
     * @param phone
     */
    private void checkPhoneUnique(String phone) {
        Integer count =baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile",phone));
        if(count>0){
            throw new PhoneNumExistException();
        }
    }

}
