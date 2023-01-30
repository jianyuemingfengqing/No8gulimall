/*
package com.learn.gmall.cart.interceptors;


import com.learn.gmall.cart.config.JwtProperties;
import com.learn.gmall.cart.pojo.UserInfo;
import com.learn.gmall.common.utils.CookieUtils;
import com.learn.gmall.common.utils.JwtUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(JwtProperties.class)
public class LoginInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<UserInfo> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    private JwtProperties properties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 获取当前用户的登录状态，从cookie中获取userKey和token
        String userKey = CookieUtils.getCookieValue(request, this.properties.getUserKey());
        String token = CookieUtils.getCookieValue(request, this.properties.getCookieName());
        if (StringUtils.isBlank(userKey)){
            // 如果userKey不存在则生成一个放入cookie
            userKey = UUID.randomUUID().toString();
            CookieUtils.setCookie(request, response, this.properties.getUserKey(), userKey, this.properties.getExpire());
        }

        // 解析token，从token中获取userId
        Long userId = null;
        if (StringUtils.isNotBlank(token)){
            try {
                Map<String, Object> map = JwtUtils.getInfoFromToken(token, this.properties.getPublicKey());
                userId = Long.valueOf(map.get("userId").toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // 通过ThreadLocal把userId和userKey传递给后续业务代码
        THREAD_LOCAL.set(new UserInfo(userId, userKey));

        // 放行
        return true;
    }

    public static UserInfo getUserInfo(){
        return THREAD_LOCAL.get();
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //System.out.println("后置方法：在handler方法执行之后执行。。。。");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //System.out.println("完成方法：在视图渲染完成之后执行。。。。");
        // 一定要手动释放threadLocal中的资源，否则可能导致内存泄漏直至服务器宕机（OOM），因为这里我们使用的是tomcat线程池，所有请求结束线程不会结束
        THREAD_LOCAL.remove();
    }
}
*/
