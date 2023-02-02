package com.learn.gmall.cart.controller;


import com.learn.gmall.cart.interceptors.LoginInterceptor;
import com.learn.gmall.cart.interceptors.LoginInterceptorTest;
import com.learn.gmall.cart.pojo.Cart;
import com.learn.gmall.cart.service.CartService;
import com.learn.gmall.common.bean.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping
    public String saveCart(Cart cart){
        // 新增购物车代码
        this.cartService.saveCart(cart);

        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId() + "&count=" + cart.getCount();
    }
    @GetMapping("addCart.html")
    public String addCart(Cart cart, Model model){ // 回显
        BigDecimal count = cart.getCount(); // 本次新增数量
        // 根据skuId查询该用户的购物车记录
        cart = this.cartService.queryCart(cart.getSkuId());// 从拦截器中获取的sku id
        cart.setCount(count);

        model.addAttribute("cart", cart);
        return "addCart";
    }

    // 查询购物车
    @GetMapping("cart.html")
    public String queryCarts(Model model){ // 参数分析, user key与 id 都是从网关中直接获取, 不需要参数, model用来返回结果集

        List<Cart> carts = this.cartService.queryCarts();
        model.addAttribute("carts", carts); // 看前端的命名

        return "cart"; // 返回结果集
    }

    @PostMapping("updateNum") // 数量
    @ResponseBody
    public ResponseVo updateNum(@RequestBody Cart cart){ //经过分析 只需要code, 所以不指定泛型

        this.cartService.updateNum(cart);
        return ResponseVo.ok();
    }
    @PostMapping("updateStatus") // 状态
    @ResponseBody
    public ResponseVo updateStatus(@RequestBody Cart cart){ //经过分析 只需要code, 所以不指定泛型

        this.cartService.updateStatus(cart);
        return ResponseVo.ok();
    }

    @PostMapping("deleteCart")
    @ResponseBody
    public ResponseVo deleteCart(@RequestParam("skuId")Long skuId){
        this.cartService.deleteCartBySkuId(skuId);
        return ResponseVo.ok();
    }

    @GetMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request) {
//        System.out.println("handle方法" + LoginInterceptorTest.userInfo); 全局

/*        //使用请求
        System.out.println("handle方法" +
                "userid: " + request.getAttribute("userId") +
                "   " +
                "userKey: " + request.getAttribute("userKey"));
        */
//        System.out.println("handle方法" + LoginInterceptorTest.getUserInfo());//局部线程
        System.out.println("这是handler方法。。。。。。。。。。。。。。。。" + LoginInterceptor.getUserInfo());
        return "test";
        /*
        long now = System.currentTimeMillis();
        System.out.println("这是controller 方法开始执行。。。");
//        ListenableFuture<String> future1 = this.cartService.execute1();
//        ListenableFuture<String> future2 = this.cartService.execute2();
        this.cartService.execute1();
        this.cartService.execute2();

//        future1.addCallback(result -> {
//            System.out.println("execute1异步任务执行成功：" + result);
//        }, ex -> {
//            System.out.println("execute1异步任务执行失败：" + ex.getMessage());
//        });
//        future2.addCallback(result -> {
//            System.out.println("execute2异步任务执行成功：" + result);
//        }, ex -> {
//            System.out.println("execute2异步任务执行失败：" + ex.getMessage());
//        });
//        try {
//            System.out.println(future1.get() + "======================" + future2.get());
//        } catch (Exception e) {
//            System.out.println("子任务的异常信息：" + e.getMessage());
//        }
        System.out.println("controller方法的执行时间：" + (System.currentTimeMillis() - now));
        return "hello test!";*/
//    }

    }


}
