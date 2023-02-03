package com.learn.gmall.scheduled.cart;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.gmall.scheduled.mapper.CartMapper;
import com.learn.gmall.scheduled.pojo.Cart;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class CartJobHandler {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CartMapper cartMapper;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String EXCEPTION_KEY = "cart:exception";
    private static final String KEY_PREFIX = "cart:info:";

    @XxlJob("cartSyncData")
    public ReturnT<String> syncData(String param){

        // 1.从redis中读取失败信息 Set<UserId>
        BoundSetOperations<String, String> setOps = this.redisTemplate.boundSetOps(EXCEPTION_KEY);
        String userId = setOps.pop();// pop移除并返回随机元素, set是无序的

        // 2.遍历失败信息
        while (StringUtils.isNotBlank(userId)){

            // 3.删除mysql中该用户的所有购物车
            this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId));

            // 4.查询redis中该用户的购物车
            BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
            List<Object> cartJsons = hashOps.values();
            if (CollectionUtils.isEmpty(cartJsons)){
                userId = setOps.pop();//获取用户id
                continue;//进入下一次循环
            }

            // 5.新增到mysql
            cartJsons.forEach(cartJson -> {

                try {
                    Cart cart = MAPPER.readValue(cartJson.toString(), Cart.class);// 反序列化

                    this.cartMapper.insert(cart);//新增数据
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });

            // 获取下一个用户
            userId = setOps.pop();
        }

        return ReturnT.SUCCESS;
    }
}
