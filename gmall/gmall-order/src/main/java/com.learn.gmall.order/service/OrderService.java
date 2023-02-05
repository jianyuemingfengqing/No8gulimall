package com.learn.gmall.order.service;


import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.learn.gmall.cart.pojo.Cart;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.common.exception.OrderException;
import com.learn.gmall.order.feign.*;
import com.learn.gmall.order.interceptors.LoginInterceptor;
import com.learn.gmall.order.pojo.OrderConfirmVo;
import com.learn.gmall.order.pojo.OrderItemVo;
import com.learn.gmall.order.pojo.UserInfo;
import com.learn.gmall.pms.entity.SkuAttrValueEntity;
import com.learn.gmall.pms.entity.SkuEntity;
import com.learn.gmall.sms.vo.ItemSaleVo;
import com.learn.gmall.ums.entity.UserAddressEntity;
import com.learn.gmall.ums.entity.UserEntity;
import com.learn.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GmallUmsClient umsClient;
    @Autowired
    private GmallSmsClient smsClient;
    @Autowired
    private GmallCartClient cartClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "order:token:";

    public OrderConfirmVo confirm() {

        OrderConfirmVo confirmVo = new OrderConfirmVo();

        // 获取登录信息
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        // 1.根据userId查询用户的收货地址列表
        ResponseVo<List<UserAddressEntity>> addressResponseVo = this.umsClient.queryAddressesByUserId(userId);
        List<UserAddressEntity> addressEntities = addressResponseVo.getData();
        confirmVo.setAddresses(addressEntities);

        // 2.根据userId查询查询已选中的购物车记录
        ResponseVo<List<Cart>> cartResponseVo = this.cartClient.queryCheckedCarts(userId);
        List<Cart> carts = cartResponseVo.getData();
        if (CollectionUtils.isEmpty(carts)) {// 没有选中
            throw new OrderException("请选择购买商品！");
        }

        confirmVo.setItems(
                carts.stream().map(
                        cart -> {
                            OrderItemVo orderItemVo = new OrderItemVo();
                            // 从购物车对象中只获取skuId和count，其他字段的数据要实时从数据库获取
                            orderItemVo.setSkuId(cart.getSkuId());
                            orderItemVo.setCount(cart.getCount());

                            // 3.根据skuId查询sku
                            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
                            SkuEntity skuEntity = skuEntityResponseVo.getData();
                            if (skuEntity != null) {
                                orderItemVo.setTitle(skuEntity.getTitle());
                                orderItemVo.setPrice(skuEntity.getPrice());
                                orderItemVo.setWeight(skuEntity.getWeight());
                                orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
                            }

                            // 4.根据skuId查询销售属性
                            ResponseVo<List<SkuAttrValueEntity>> saleAttrsResponseVo = this.pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
                            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrsResponseVo.getData();
                            orderItemVo.setSaleAttrs(skuAttrValueEntities);

                            // 5.根据skuId查询库存
                            ResponseVo<List<WareSkuEntity>> wareResponseVo = this.wmsClient.queryWareSkusBySkuId(cart.getSkuId());
                            List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                                orderItemVo.setStore(
                                        wareSkuEntities.stream()
                                                .anyMatch(
                                                        wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                                                )
                                );
                            }

                            // 6.根据skuId查询营销信息
                            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
                            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
                            orderItemVo.setSales(itemSaleVos);

                            return orderItemVo;
                        }
                ).collect(Collectors.toList()));

        // 7.根据userId查询用户积分
        ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUserById(userId);
        UserEntity userEntity = userEntityResponseVo.getData();
        if (userEntity != null) {
            confirmVo.setBounds(userEntity.getIntegration());
        }

        // 生成orderToken 页面 redis
        String orderToken = IdWorker.getIdStr();
        confirmVo.setOrderToken(orderToken);
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken);

        return confirmVo;
    }

}
