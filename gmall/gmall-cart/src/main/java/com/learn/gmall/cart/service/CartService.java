package com.learn.gmall.cart.service;

import com.alibaba.fastjson.JSON;
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
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
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
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
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
        if (hashOps.hasKey(skuId.toString())) { // 需要在缓存中存在再去查询, 可以避免改url就显示新增成功
            String cartJson = hashOps.get(skuId.toString()).toString();
            return JSON.parseObject(cartJson, Cart.class);
        }
        throw new CartException("你的购物车没有该记录！");
    }


    @Async
    public void execute1() {
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
    public void execute2() {
        try {
            System.out.println("execute2方法开始执行。。。。");
            TimeUnit.SECONDS.sleep(4);
            int i = 1 / 0;
            System.out.println("execute2方法结束执行==========================");
        } catch (InterruptedException e) {
            //return AsyncResult.forExecutionException(e);
        }
        //return AsyncResult.forValue("hello execute2");
    }

    public List<Cart> queryCarts() {

        // 获取登录信息中的userKey
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String userKey = userInfo.getUserKey();

        // 1.根据userKey查询未登录的购物车 Map<skuId, CartJson>
        BoundHashOperations<String, Object, Object> unloginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userKey);
        // 获取未登录购物车的列表
        List<Object> cartJsons = unloginHashOps.values();
        // 把未登录的购物车json字符串集合 反序列化为 购物车对象集合
        List<Cart> unloginCarts = null; //未登录用户 的购物车

        if (!CollectionUtils.isEmpty(cartJsons)) { // 防止空存
            unloginCarts = cartJsons.stream().map(
                    cartJson -> {
                        Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                        // 查询购物车时，查询实时价格缓存
//                        cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                        return cart;
                    }
            ).collect(Collectors.toList());
        }

        // 2.判断登录状态（userId==null），说明未登录则直接返回未登录的购物车
        Long userId = userInfo.getUserId();
        if (userId == null) {
            return unloginCarts;
        }

        // 3.根据userId查询已登录的购物车
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);

        // 4.把未登录的购物车 合并到 已登录的购物车中去
        if (!CollectionUtils.isEmpty(unloginCarts)) { // 如果未登录的购物车不为空则合并
            // 遍历未登录的购物车，判断已登录的购物车中是否包含该记录
            unloginCarts.forEach(
                    cart -> { // 每一条未登录购物车对象
                        String skuId = cart.getSkuId().toString();
                        BigDecimal count = cart.getCount(); // 未登录购物车的数量
                        if (loginHashOps.hasKey(skuId)) {
                            // 包含 则数量累加
                            String cartJson = loginHashOps.get(skuId).toString();
                            cart = JSON.parseObject(cartJson, Cart.class);
                            cart.setCount(cart.getCount().add(count)); // 会覆盖数量, 覆盖之前要获取
                            // 写入数据库
                            this.asyncService.updateCart(userId.toString(), skuId, cart);
                        } else {
                            // 不包含则新增记录
                            cart.setUserId(userId.toString());
                            cart.setId(null); // 为了防止主键冲突，把id设置为null, 让其自增
                            this.asyncService.insertCart(cart);
                        }
                        loginHashOps.put(skuId, JSON.toJSONString(cart)); // 无论如何都要同步到redis中
                    }
            );

            // 5.清空未登录的购物车
            this.redisTemplate.delete(KEY_PREFIX + userKey); // 清空redis
            this.asyncService.deleteCart(userKey); // 删除mysql
        }

        // 6.返回合并后的购物车
        List<Object> loginCartJsons = loginHashOps.values();
        if (!CollectionUtils.isEmpty(loginCartJsons)) {
            return loginCartJsons.stream().map(
                    cartJson -> {
                        Cart cart = JSON.parseObject(cartJson.toString(), Cart.class);
                        // 查询购物车时查询实时价格缓存
//                        cart.setCurrentPrice(new BigDecimal(this.redisTemplate.opsForValue().get(PRICE_PREFIX + cart.getSkuId())));
                        return cart;
                    }
            ).collect(Collectors.toList());
        }
        return null;
    }

    public void updateNum(Cart cart) {

        String userId = this.getUserId(); // 要么 key 要么 id

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        // 可以用swagger 或者 postman传数据
        String skuId = cart.getSkuId().toString();
        BigDecimal count = cart.getCount(); // 要更新的数量, 前端传来的
        if (hashOps.hasKey(skuId)) { // redis中已经有了
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCount(count); // 覆盖掉

            hashOps.put(skuId, JSON.toJSONString(cart)); // 更新redis
            this.asyncService.updateCart(userId, skuId, cart); //更新mysql
        }
    }

    public void deleteCartBySkuId(Long skuId) {

        String userId = this.getUserId();

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        hashOps.delete(skuId.toString());
        this.asyncService.deleteCartByUserIdAndSkuId(userId, skuId);
    }

    public void updateStatus(Cart cart) {
        String userId = this.getUserId(); // 要么 key 要么 id

        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        // 可以用swagger 或者 postman传数据
        String skuId = cart.getSkuId().toString();
        Boolean check = cart.getCheck();// 要更新的是否选中
        if (hashOps.hasKey(skuId)) { // redis中已经有了
            String cartJson = hashOps.get(skuId).toString();
            cart = JSON.parseObject(cartJson, Cart.class);
            cart.setCheck(check); // 覆盖掉

            hashOps.put(skuId, JSON.toJSONString(cart)); // 更新redis
            this.asyncService.updateCart(userId, skuId, cart); //更新mysql
        }
    }
}
