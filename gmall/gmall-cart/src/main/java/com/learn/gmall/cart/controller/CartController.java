package com.learn.gmall.cart.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CartController {

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

        return "test";
    }
/*        //System.out.println("这是handler方法。。。。。。。。。。。。。。。。" + LoginInterceptor.getUserInfo());
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

/*    @Autowired
    private CartService cartService;

    @GetMapping
    public String saveCart(Cart cart){
        // 新增购物车代码
        this.cartService.saveCart(cart);

        return "redirect:http://cart.gmall.com/addCart.html?skuId=" + cart.getSkuId() + "&count=" + cart.getCount();
    }

    @GetMapping("addCart.html")
    public String addCart(Cart cart, Model model){

        BigDecimal count = cart.getCount(); // 本次新增数量
        // 根据skuId查询该用户的购物车记录
        cart = this.cartService.queryCart(cart.getSkuId());
        cart.setCount(count);

        model.addAttribute("cart", cart);

        return "addCart";
    }

 */
}
