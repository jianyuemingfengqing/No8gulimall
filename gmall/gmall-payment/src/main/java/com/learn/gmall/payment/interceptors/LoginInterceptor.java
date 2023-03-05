package com.learn.gmall.payment.interceptors;

import com.learn.gmall.payment.pojo.UserInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    // public static UserInfo userInfo;

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        Long userId = Long.valueOf(request.getHeader("userId"));
        String username = request.getHeader("username");

        // 通过ThreadLocal把userId和userKey传递给后续业务代码
        THREAD_LOCAL.set(new UserInfo(userId, null, username));

        // 放行
        return true;
    }

    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //System.out.println("完成方法：在视图渲染完成之后执行。。。。");
        // 一定要手动释放threadLocal中的资源，否则可能导致内存泄漏直至服务器宕机（OOM），因为这里我们使用的是tomcat线程池，所有请求结束线程不会结束
        THREAD_LOCAL.remove();
    }
}
