package com.learn.gmall.cart.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;

@Slf4j
@Component
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
    // 记录异常信息
    private static final String EXCEPTION_KEY = "cart:exception";

    @Resource
    private StringRedisTemplate stringRedisTemplate ;
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.error("异步任务执行失败。原因：{}，方法：{}，参数：{}", throwable.getMessage(), method.getName(), Arrays.asList(objects));

        BoundSetOperations<String, String> setOps = stringRedisTemplate.boundSetOps(EXCEPTION_KEY);//
        setOps.add(objects[0].toString()); //存储信息  需要userid, 按照约定
    }
}
