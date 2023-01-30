package com.learn.gmall.cart.config;

//import com.learn.gmall.cart.interceptors.LoginInterceptor;
import com.learn.gmall.cart.interceptors.LoginInterceptorTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Autowired
//    private LoginInterceptor loginInterceptor;
    private LoginInterceptorTest loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
    }
}
