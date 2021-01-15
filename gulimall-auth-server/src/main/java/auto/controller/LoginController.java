package auto.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gulimall.common.constant.AuthServerConstant;
import com.atguigu.gulimall.common.utils.BizCodeEnum;;
import com.atguigu.gulimall.common.utils.R;
import com.atguigu.gulimall.common.vo.MemberResponseVo;
import auto.feign.MemberFeignService;
import auto.feign.ThirdPartFeignService;
import auto.vo.UserLoginVo;
import auto.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController {
    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;


    @RequestMapping("/login.html")
    public String loginPage(HttpSession session) {
        if (session.getAttribute(AuthServerConstant.LOGIN_USER) != null) {
            return "redirect:http://127.0.0.1:20000/login.html";
        } else {
            return "login";
        }
    }

    @RequestMapping("/login")
    public String login(UserLoginVo vo, RedirectAttributes attributes, HttpSession session) {
        R r = memberFeignService.login(vo);
        if (r.getCode() == 0) {
            String jsonString = JSON.toJSONString(r.get("memberEntity"));
            MemberResponseVo memberResponseVo = JSON.parseObject(jsonString, new TypeReference<MemberResponseVo>() {
            });
            session.setAttribute(AuthServerConstant.LOGIN_USER, memberResponseVo);
            return "redirect:http://127.0.0.1:9100/";
        } else {
            String msg = (String) r.get("msg");
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", msg);
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://127.0.0.1:20000/login.html";
        }
    }


    @GetMapping("/sms/sendCode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone) {
        //接口防刷,在redis中缓存phone-code
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String prePhone = AuthServerConstant.SMS_CODE_CACHE_PREFIX + phone;
        String v = ops.get(prePhone);
        if (!StringUtils.isEmpty(v)) {
            long pre = Long.parseLong(v.split("_")[1]);
            //如果存储的时间小于60s，说明60s内发送过验证码
            if (System.currentTimeMillis() - pre < 60000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }
        //如果存在的话，删除之前的验证码
        redisTemplate.delete(prePhone);
        //获取到6位数字的验证码
        String code = String.valueOf((int) ((Math.random() + 1) * 100000));
        //在redis中进行存储并设置过期时间
        ops.set(prePhone, code + "_" + System.currentTimeMillis(), 10, TimeUnit.MINUTES);
        thirdPartFeignService.sendCode(phone, code);
        System.out.println("phone=" + phone + "code=" + code);
        return R.ok();
    }

    /**
     * 注册会员
     * RedirectAttributes将信息封装到sessioin中
     *
     * @param registerVo
     * @param result
     * @param attributes
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo registerVo, BindingResult result, RedirectAttributes attributes) {
        //1.校验数据格式是否正确
        //思路：将错误类型设置成map类型
        Map<String, String> errors = new HashMap<>();
        //1.1 如果校验不通过，则封装校验结果
        result.getFieldErrors().forEach(item -> {
            errors.put(item.getField(), item.getDefaultMessage());
            //1.2 将错误信息封装到session中
            attributes.addFlashAttribute("errors", errors);
        });
        //2.若JSR303校验通过
        //判断验证码是否正确
        //2.1 如果对应手机的验证码不为空且与提交上的相等-》验证码正确
        String code = redisTemplate.opsForValue().get(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());
        if (!StringUtils.isEmpty(code) && registerVo.getCode().equals(code.split("_")[0])) {
            //2.1.1似的验证后的验证码失效
            redisTemplate.delete(AuthServerConstant.SMS_CODE_CACHE_PREFIX + registerVo.getPhone());
            //2.1.2 远程调用会员服务注册
            R r = memberFeignService.register(registerVo);
            if (r.getCode() == 0) {
                //调用成功，重定向登录页
                return "redirect:http://auth.gulimall.com/login.html";
            } else {
                //调用失败，返回注册页并显示错误信息
                String msg = (String) r.get("msg");
                errors.put("msg", msg);
                attributes.addFlashAttribute("errors", errors);
                return "redirect:http://auth.gulimall.com/reg.html";
            }
        } else {
            //2.2 验证码错误
            errors.put("code", "验证码错误");
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/reg.html";
        }

    }

}
