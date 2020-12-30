package com.atguigu.gulimall.order.interceptor;

import com.atguigu.gulimall.common.constant.AuthServerConstant;
import com.atguigu.gulimall.common.vo.MemberResponseVo;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @ClassName LoginInterceptor
 * @Description: 登录拦截器，未登录的用户无法访问订单服务
 * @Author fengjc
 * @Date 2020/12/30
 * @Version V1.0
 **/
public class LoginInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberResponseVo> loginLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        AntPathMatcher matcher = new AntPathMatcher();
        boolean match1 = matcher.match("/order/order/infoByOrderSn/**", requestURI);
        boolean match2 = matcher.match("/payed/**", requestURI);
        if (match1 || match2) {
            return true;
        }
        HttpSession session = request.getSession();
        MemberResponseVo responseVo = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
        if (responseVo != null) {
            loginLocal.set(responseVo);
            return true;
        } else {
            session.setAttribute("msg", "请先登录");
            response.sendRedirect("http://auth.gulimall.com/login.html");
            return true;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }
}
