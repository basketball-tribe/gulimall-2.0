package com.atguigu.gulimall.thirdparty.controller;

import com.atguigu.gulimall.common.constant.AuthServerConstant;
import com.atguigu.gulimall.common.utils.BizCodeEnum;
import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.thirdparty.component.SmsComponent;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName SmsController
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/23
 * @Version V1.0
 **/
@RestController
@RequestMapping(value = "/sms")
public class SmsController {
    @Resource
    private SmsComponent smsComponent;


    /**
     * 提供给别的服务进行调用
     *
     * @param phone
     * @param code
     * @return
     */
    @ResponseBody
    @GetMapping(value = "/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {

        //发送验证码
        smsComponent.sendCode(phone, code);
        System.out.println(phone + code);
        return R.ok();
    }
}
