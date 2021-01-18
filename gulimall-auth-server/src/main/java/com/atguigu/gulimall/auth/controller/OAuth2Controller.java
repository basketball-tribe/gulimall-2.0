package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.common.constant.AuthServerConstant;
import com.atguigu.gulimall.common.utils.HttpUtils;
import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.auth.feignn.MemberFeignService;
import com.atguigu.gulimall.common.vo.MemberResponseVo;
import com.atguigu.gulimall.auth.vo.SocialUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * User: haitao
 * Date: 2020/7/11
 * 处理社交登录请求
 */

@Controller
@Slf4j
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 社交登录成功页面
     *
     * @param code
     * @return
     * @throws Exception
     */
    // http://auth.gulimall.com/oauth2.0/weibo/success
    // http://auth.gulimall.com/oauth2.0/weibo/success?code=b73fe58bee29bdac992728d5a49549f8
    @GetMapping("/oauth2.0/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception {
        // 根据code 获取accessToken  post 请求

        // 根据code换取accessToken
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "2460608311");
        map.put("client_secret", "3eec8f7e3d89d65d6cbbfaef016ce194");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        map.put("code", code);
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<String, String>(), null, map);

        // 2、处理响应数据
        if (response.getStatusLine().getStatusCode() == 200) {
            // 获取到了accessToken
            String json = EntityUtils.toString(response.getEntity()); // 将响应的内容转换成json字符串
            SocialUserVo socialUserVo = JSON.parseObject(json, SocialUserVo.class); // 将获取到的json转换成SocialUserVo对象
            // 知道当前是哪个社交用户
            // 1、会员第一次登陆，自动注册（为当前社交用户生成一个会员信息账号，以后这个社交账号就对应指定的会员信息）
            // 调用远程服务 gulimall-member 登陆或注册操作
            R r = memberFeignService.oauthlogin(socialUserVo);
            if (r.getCode() == 0) {
                MemberResponseVo MemberResponseVo = r.getData(new TypeReference<MemberResponseVo>() {
                });
                log.info("登录成功,用户：{}", MemberResponseVo);
                session.setAttribute(AuthServerConstant.LOGIN_USER, MemberResponseVo);
//                servletResponse.addCookie(new Cookie("JSESSIONID","xxxx").setDomain("gulimall.com"););
                // 第一次使用session:命令浏览器保存卡号（就是响应头中setCookie有值）。JSESSOINID这个cookie；
                // 以后浏览器访问那个网站就会带上这个网站的cookie（cookie是保存在浏览器中的，基于域名查找）
                // TODO 1、默认发的令牌。session=xxxx。作用域就是当前请求头里面的Host的值
                // TODO 2、使用JSON序列化的方式来序列化对象数据保存到redis中（这样子就不需要每个类都写上Serializable）
                // 登录成功跳转到首页
                return "redirect:http://gulimall.com";
            }
            return "redirect:http://auth.gulimall.com/login.html";
        } else { // 远程请求社交服务获取数据失败
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }
}
