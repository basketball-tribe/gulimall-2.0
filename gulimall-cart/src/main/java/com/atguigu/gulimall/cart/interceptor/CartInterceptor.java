package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.gulimall.cart.to.UserInfoTo;
import com.atguigu.gulimall.common.constant.AuthServerConstant;
import com.atguigu.gulimall.common.constant.CartConstant;
import com.atguigu.gulimall.common.vo.MemberResponseVo;
import org.apache.catalina.User;
import org.apache.commons.lang.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @ClassName CartInterceptor
 * @Description: TODO
 * @Author fengjc
 * @Date 2020/12/30
 * @Version V1.0
 **/
public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

    /**
     *参考京东，在点击购物车时，会为临时用户生成一个name为user-key的cookie临时标识，过期时间为一个月，如果手动清除user-key，
     * 那么临时购物车的购物项也被清除，所以user-key是用来标识和存储临时购物车数据的
     *
     * 使用ThreadLocal进行用户身份鉴别信息传递
     * 在调用购物车的接口前，先通过session信息判断是否登录，并分别进行用户身份信息的封装，并把user-key放在cookie中
     * 这个功能使用拦截器进行完成
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        MemberResponseVo memberResponseVo = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        UserInfoTo userInfoTo = new UserInfoTo();
        if (memberResponseVo != null) {
            //如果用户已登录，设置userid
            userInfoTo.setUserId(memberResponseVo.getId());
        }
        //从cookie中获取信息
        //如果cookie中已经有user-Key，则直接设置
        //如果没有通过uuid生成user-key
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(cookie.getName())) {
                    //2 如果浏览器中已经有user-Key，则直接设置
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            userInfoTo.setUserKey(UUID.randomUUID().toString());
        }
        threadLocal.set(userInfoTo);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        UserInfoTo userInfoTo = threadLocal.get();
        //如果浏览器中没有user-key，我们为其生成
        if (!userInfoTo.getTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }
}
