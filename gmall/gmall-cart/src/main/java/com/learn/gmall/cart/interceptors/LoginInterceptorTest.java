package com.learn.gmall.cart.interceptors;

import com.learn.gmall.cart.pojo.UserInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class LoginInterceptorTest implements HandlerInterceptor {
//    public static UserInfo userInfo; // 初始化对象

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("前置");

        // 解析后的数据
        Long userId = 4L;
        String userKey = UUID.randomUUID().toString();
    /*    // 使用对象传递
        userInfo = new UserInfo(userId, userKey); // 一般来说 在此处初始化对象*/
/*
        // 使用请求来传递
        request.setAttribute("userId", userId);
        request.setAttribute("userKey", userKey);
*/

        return true; // true 放行  false拦截
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        System.out.println("后置");

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        System.out.println("完成");
    }
}
