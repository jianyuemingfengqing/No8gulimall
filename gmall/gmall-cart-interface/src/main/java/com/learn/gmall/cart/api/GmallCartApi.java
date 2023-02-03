package com.learn.gmall.cart.api;

import com.learn.gmall.cart.pojo.Cart;
import com.learn.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

public interface GmallCartApi {

    @GetMapping("user/{userId}")
    @ResponseBody
    ResponseVo<List<Cart>> queryCheckedCarts(@PathVariable("userId")Long userId);
}
