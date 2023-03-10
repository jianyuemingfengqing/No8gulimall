package com.learn.gmall.order.service;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.learn.gmall.cart.pojo.Cart;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.common.exception.OrderException;
import com.learn.gmall.oms.vo.OrderItemVo;
import com.learn.gmall.oms.vo.OrderSubmitVo;
import com.learn.gmall.order.feign.*;
import com.learn.gmall.order.interceptors.LoginInterceptor;
import com.learn.gmall.order.pojo.OrderConfirmVo;
import com.learn.gmall.order.pojo.UserInfo;
import com.learn.gmall.pms.entity.SkuAttrValueEntity;
import com.learn.gmall.pms.entity.SkuEntity;
import com.learn.gmall.sms.vo.ItemSaleVo;
import com.learn.gmall.ums.entity.UserAddressEntity;
import com.learn.gmall.ums.entity.UserEntity;
import com.learn.gmall.wms.entity.WareSkuEntity;
import com.learn.gmall.wms.vo.SkuLockVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private GmallOmsClient omsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String KEY_PREFIX = "order:token:";

    public OrderConfirmVo confirm() {

        OrderConfirmVo confirmVo = new OrderConfirmVo();

        // ??????????????????
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();

        // 1.??????userId?????????????????????????????????
        ResponseVo<List<UserAddressEntity>> addressResponseVo = this.umsClient.queryAddressesByUserId(userId);
        List<UserAddressEntity> addressEntities = addressResponseVo.getData();
        confirmVo.setAddresses(addressEntities);

        // 2.??????userId???????????????????????????????????????
        ResponseVo<List<Cart>> cartResponseVo = this.cartClient.queryCheckedCarts(userId);
        List<Cart> carts = cartResponseVo.getData();
        if (CollectionUtils.isEmpty(carts)) {// ????????????
            throw new OrderException("????????????????????????");
        }

        confirmVo.setItems(
                carts.stream().map(
                        cart -> {
                            OrderItemVo orderItemVo = new OrderItemVo();
                            // ??????????????????????????????skuId???count???????????????????????????????????????????????????
                            orderItemVo.setSkuId(cart.getSkuId());
                            orderItemVo.setCount(cart.getCount());

                            // 3.??????skuId??????sku
                            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(cart.getSkuId());
                            SkuEntity skuEntity = skuEntityResponseVo.getData();
                            if (skuEntity != null) {
                                orderItemVo.setTitle(skuEntity.getTitle());
                                orderItemVo.setPrice(skuEntity.getPrice());
                                orderItemVo.setWeight(skuEntity.getWeight());
                                orderItemVo.setDefaultImage(skuEntity.getDefaultImage());
                            }

                            // 4.??????skuId??????????????????
                            ResponseVo<List<SkuAttrValueEntity>> saleAttrsResponseVo = this.pmsClient.querySaleAttrValuesBySkuId(cart.getSkuId());
                            List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrsResponseVo.getData();
                            orderItemVo.setSaleAttrs(skuAttrValueEntities);

                            // 5.??????skuId????????????
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

                            // 6.??????skuId??????????????????
                            ResponseVo<List<ItemSaleVo>> salesResponseVo = this.smsClient.querySalesBySkuId(cart.getSkuId());
                            List<ItemSaleVo> itemSaleVos = salesResponseVo.getData();
                            orderItemVo.setSales(itemSaleVos);

                            return orderItemVo;
                        }
                ).collect(Collectors.toList()));

        // 7.??????userId??????????????????
        ResponseVo<UserEntity> userEntityResponseVo = this.umsClient.queryUserById(userId);
        UserEntity userEntity = userEntityResponseVo.getData();
        if (userEntity != null) {
            confirmVo.setBounds(userEntity.getIntegration());
        }

        // ??????orderToken ?????? redis
        String orderToken = IdWorker.getIdStr();
        confirmVo.setOrderToken(orderToken);
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, orderToken);

        return confirmVo;
    }

    public void submit(OrderSubmitVo submitVo) {

        // 1.????????????????????????orderToken???redis????????????????????????????????????????????????????????????
        String orderToken = submitVo.getOrderToken(); // ????????????orderToken
        if (StringUtils.isBlank(orderToken)) {
            throw new OrderException("???????????????");
        }
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] " +
                "then " +
                "   return redis.call('del', KEYS[1]) " +
                "else " +
                "   return 0 " +
                "end";
        Boolean flag = this.redisTemplate.execute(
                new DefaultRedisScript<>(script, Boolean.class),
                Arrays.asList(KEY_PREFIX + orderToken),
                orderToken
        );
        if (!flag) {
            throw new OrderException("????????????????????????");
        }

        // 2.??????????????????????????????????????? ??? ??????????????????????????? ??????????????????????????????????????????
        List<OrderItemVo> items = submitVo.getItems(); // ????????????
        if (CollectionUtils.isEmpty(items)) {
            throw new OrderException("??????????????????????????????");
        }
        BigDecimal totalPrice = submitVo.getTotalPrice(); // ???????????????
        // ?????????????????????
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            // ??????item???skuId??????????????????
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return new BigDecimal(0);
            }
            return skuEntity.getPrice().multiply(item.getCount()); // ????????????
        }).reduce((a, b) -> a.add(b)).get();
        if (currentTotalPrice.compareTo(totalPrice) != 0) { // ?????????
            throw new OrderException("??????????????????");
        }

        // TODO: ????????????

        // 3.?????????????????????????????????????????????
        ResponseVo<List<SkuLockVo>> skuLockVosResponseVo = this.wmsClient.checkLock(
                items.stream().map(
                        item -> {
                            SkuLockVo skuLockVo = new SkuLockVo();

                            skuLockVo.setSkuId(item.getSkuId());
                            skuLockVo.setCount(item.getCount().intValue());

                            return skuLockVo;
                        }
                ).collect(Collectors.toList()), orderToken
        );

        List<SkuLockVo> skuLockVos = skuLockVosResponseVo.getData();
        if (!CollectionUtils.isEmpty(skuLockVos)) {//???????????? ??????, ????????????
            throw new OrderException(JSON.toJSONString(skuLockVos));
        }

        // 4.????????????
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        try {
            this.omsClient.saveOrder(submitVo, userId);
            this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE", "order.ttl", orderToken);//????????????
        } catch (Exception e) {
            e.printStackTrace();
            // ??????????????????????????????????????????wms????????????????????????oms?????????????????????
            this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE", "order.fail", orderToken);
            throw new OrderException("????????????????????????");
        }

        // 5.??????????????????????????????????????????MQ???
        Map<String, Object> msg = new HashMap<>();
        msg.put("userId", userId);
        msg.put("skuIds",
                JSON.toJSONString(
                        items.stream()
                                .map(OrderItemVo::getSkuId)
                                .collect(Collectors.toList())
                )
        );
        this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE", "cart.delete", msg);
    }
}
