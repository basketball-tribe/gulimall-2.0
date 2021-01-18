package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.common.constant.AuthServerConstant;
import com.atguigu.gulimall.common.excetion.BizCodeEnume;
import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.common.vo.MemberResponseVo;
import com.atguigu.gulimall.auth.feignn.MemberFeignService;
import com.atguigu.gulimall.auth.feignn.ThirdPartFeignService;
import com.atguigu.gulimall.auth.vo.UserLoginVo;
import com.atguigu.gulimall.auth.vo.UserRegisVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * User: haitao
 * Date: 2020/7/9
 */
@Controller
public class LoginController {
    @Autowired
    ThirdPartFeignService thirdPartFeignService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    /**
     * 获取短信验证码
     *
     * @param phone
     * @return
     */
    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        // TODO 接口防刷

        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(s)) {
            // 60秒才能再次给同一手机号发送验证码
            if (System.currentTimeMillis() - Long.parseLong(s.split("_")[1]) < 60000) {
                return R.error(BizCodeEnume.VAILD_SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.VAILD_SMS_CODE_EXCEPTION.getMsg());
            }
        }
        // 发送短信验证码
        String code = UUID.randomUUID().toString().substring(0, 5) + "_" + System.currentTimeMillis();
        // 验证码有效期是 10 分钟
        stringRedisTemplate.opsForValue().set(AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone, code, 10, TimeUnit.MINUTES);
        R r = thirdPartFeignService.sendCode(phone, code.split("_")[0]);
        return R.ok();
    }

    /**
     * TODO 重定向携带数据，利用session原理，将数据放在session中。
     * 只要跳到下一个页面取出数据以后，session里面的数据就会被删掉。
     * <p>
     * TODO 分布式session问题
     * <p>
     * RedirectAttributes 模拟重定向携带数据
     *
     * @param userRegisVo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regist")
    public String regist(@Valid UserRegisVo userRegisVo, BindingResult result, RedirectAttributes redirectAttributes) {
        Map<String, String> map = new HashMap<>();
        if (result.hasErrors()) {
            result.getFieldErrors().stream().forEach(field -> map.put(field.getField(), field.getDefaultMessage()));

            redirectAttributes.addFlashAttribute("errors", map); // 这个属性表示只能取一次
            // 校验出错，转发到注册页面
            // Request method 'POST' not supported
            // 用户注册 -> regist[post] -> 转发/reg.html(路径映射默认都是get方式访问的)
//            return "forward:/reg.html";
            // 转发会出现表单重复提交问题（刷新页面）
//            return "reg";
            // 重定向数据共享问题，springmvc 给我们解决了
            // 清空错误信息
            return "redirect:http://auth.gulimall.com/reg.html";
        }
        // 校验验证码
        String s = stringRedisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisVo.getPhone());
        if (StringUtils.isEmpty(s)) {
            // 验证码不存在
            map.put("code", "验证码不存在");
            redirectAttributes.addFlashAttribute("errors", map);
            return "redirect:http://auth.gulimall.com/reg.html";
        } else {
            if (!s.split("_")[0].equalsIgnoreCase(userRegisVo.getCode())) {
                // 验证码不正确
                map.put("code", "验证码不正确");
                redirectAttributes.addFlashAttribute("errors", map);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        }
        // 删除验证码[令牌机制]
        stringRedisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + userRegisVo.getPhone());

        // 真正注册，调用远程服务
        R r = memberFeignService.regist(userRegisVo);
        if (r.getCode() != 0) {
            // 注册失败
            map.put("msg", r.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", map);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

        // 注册成功返回到首页，回到登录页
        return "redirect:http://auth.gulimall.com/login.html";
    }

    @PostMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes redirectAttributes, HttpSession session, HttpServletRequest request) {
        // 登录请求
        R r = memberFeignService.login(vo);
        if (r.getCode() == 0) {
            String jsonString = JSON.toJSONString(r.get("memberEntity"));
            MemberResponseVo memberResponseVo = JSON.parseObject(jsonString, new TypeReference<MemberResponseVo>() {
            });

            session.setAttribute(AuthServerConstant.LOGIN_USER,memberResponseVo);
            // 登录成功
            return "redirect:http://gulimall.com/index.html";
        }
        // 登录失败
        String msg = r.getData("msg",new TypeReference<String>(){});
        Map<String, String> errors = new HashMap<>();
        errors.put("msg",msg);
        redirectAttributes.addFlashAttribute("errors", errors);
        return "redirect://auth.gulimall.com/login.html";
    }

    @GetMapping("login.html")
    public String loginPage(HttpSession session) {
        MemberResponseVo attribute = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (attribute == null) {
            // 未登录去登录页面
            return "login";
        }
        // 已登录去搜索页
        return "redirect:http://gulimall.com";
    }
}
