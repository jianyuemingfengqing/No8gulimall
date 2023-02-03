package com.learn.gmall.cart.service;


import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.learn.gmall.cart.mapper.CartMapper;
import com.learn.gmall.cart.pojo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CartAsyncService {

    @Autowired
    private CartMapper cartMapper;

    @Async
    public void updateCart(String userId, String skuId, Cart cart){
//        int i = 1/0;  //使其redis成功 mysql失败
        this.cartMapper.update(cart, new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }

    @Async
    public void insertCart(String userId,Cart cart){
        this.cartMapper.insert(cart);
    }

    @Async
    public void deleteCart(String userKeyOrUserId) { // 传来的参数是 userKey或者是userId
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userKeyOrUserId));
    }

    @Async
    public void deleteCartByUserIdAndSkuId(String userId, Long skuId) {
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId).eq("sku_id", skuId));
    }
}
