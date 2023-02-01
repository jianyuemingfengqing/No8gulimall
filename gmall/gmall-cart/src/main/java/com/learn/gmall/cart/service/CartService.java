package com.learn.gmall.cart.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.learn.gmall.cart.feign.GmallPmsClient;
import com.learn.gmall.cart.feign.GmallSmsClient;
import com.learn.gmall.cart.feign.GmallWmsClient;
import com.learn.gmall.cart.interceptors.LoginInterceptor;
import com.learn.gmall.cart.pojo.Cart;
import com.learn.gmall.cart.pojo.UserInfo;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.common.exception.CartException;
import com.learn.gmall.pms.entity.SkuAttrValueEntity;
import com.learn.gmall.pms.entity.SkuEntity;
import com.learn.gmall.sms.vo.ItemSaleVo;
import com.learn.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartAsyncService asyncService;

    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallWmsClient wmsClient;

    private static final String KEY_PREFIX = "cart:info:";
    private static final String PRICE_PREFIX = "cart:price:";

    public void saveCart(Cart cart) {

        // 1.获取登录状态：登录-userId 未登录-userKey
        String userId = getUserId();

        // 2.判断当前用户的购物车是否包含该商品 opsForHash  Map<skuId, CartJson>
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        String skuId = cart.getSkuId().toString();  // 由于是用字符串操作, 所以需要转变数据类型
        BigDecimal count = cart.getCount(); // 本次新增的数量
        if (hashOps.hasKey(skuId)) {
            // 包含-更新数量
            String cartJson = hashOps.get(skuId).toString(); // 获取字符串
            cart = JSON.parseObject(cartJson, Cart.class); // 反序列化为对象
            cart.setCount(cart.getCount().add(count)); // 数据库 + 本次
            // 写入到数据库：redis  mysql

            this.asyncService.updateCart(userId, skuId, cart);
        } else {
            // 不包含-新增记录
            cart.setUserId(userId);
            cart.setCheck(true);

            // 根据skuId查询sku
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                throw new CartException("你要加入购物车的商品不存在！");
            }
            cart.setTitle(skuEntity.getTitle());
            cart.setDefaultImage(skuEntity.getDefaultImage());
            cart.setPrice(skuEntity.getPrice());

            // 库存
            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)){
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0));
            }

            // 销售属性
            ResponseVo<List<SkuAttrValueEntity>> saleAttrsResponseVo = this.pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrsResponseVo.getData();
            cart.setSaleAttrs(JSON.toJSONString(skuAttrValueEntities));

            // 营销信息
            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
            cart.setSales(JSON.toJSONString(itemSaleVos));

            this.asyncService.insertCart(cart);
            // 加入实时价格缓存
            this.redisTemplate.opsForValue().set(PRICE_PREFIX + skuId, skuEntity.getPrice().toString());
        }
        hashOps.put(skuId, JSON.toJSONString(cart));
    }

    private String getUserId() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userId = userInfo.getUserKey(); // 定义一个userId默认取userKey
        if (userInfo.getUserId() != null) {
            // 如果userId不为空则取userId，否则取userKey
            userId = userInfo.getUserId().toString();
        }
        return userId;
    }

    public Cart queryCart(Long skuId) {
        String userId = this.getUserId();

        // 获取当前用户的购物车Map<skuId, cartJson>
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        // 获取对应的购物车记录
        if (hashOps.hasKey(skuId.toString())){ // 需要在缓存中存在再去查询, 可以避免改url就显示新增成功
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        }
        throw new CartException("你的购物车没有该记录！");
    }


    @Async
    public void execute1(){
        try {
            System.out.println("execute1方法开始执行。。。。");
            TimeUnit.SECONDS.sleep(5);
            System.out.println("execute1方法结束执行==========================");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //return AsyncResult.forValue("hello execute1");
    }

    @Async
    public void execute2(){
        try {
            System.out.println("execute2方法开始执行。。。。");
            TimeUnit.SECONDS.sleep(4);
            int i = 1/0;
            System.out.println("execute2方法结束执行==========================");
        } catch (InterruptedException e) {
            //return AsyncResult.forExecutionException(e);
        }
        //return AsyncResult.forValue("hello execute2");
    }


}
