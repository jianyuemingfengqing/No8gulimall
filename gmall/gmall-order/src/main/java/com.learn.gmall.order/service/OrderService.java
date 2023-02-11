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

    public void submit(OrderSubmitVo submitVo) {

        // 1.防重：根据页面的orderToken到redis中查询，如果存在则放行，否则直接抛出异常
        String orderToken = submitVo.getOrderToken(); // 页面中的orderToken
        if (StringUtils.isBlank(orderToken)) {
            throw new OrderException("非法请求！");
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
            throw new OrderException("请不要重复提交！");
        }

        // 2.验总价：页面中提交的总价格 和 数据库中实时总价格 比较，如果不一致直接抛出异常
        List<OrderItemVo> items = submitVo.getItems(); // 送货清单
        if (CollectionUtils.isEmpty(items)) {
            throw new OrderException("请选择要购买的商品！");
        }
        BigDecimal totalPrice = submitVo.getTotalPrice(); // 页面总价格
        // 计算实时总价格
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            // 根据item中skuId查询实时单价
            ResponseVo<SkuEntity> skuEntityResponseVo = this.pmsClient.querySkuById(item.getSkuId());
            SkuEntity skuEntity = skuEntityResponseVo.getData();
            if (skuEntity == null) {
                return new BigDecimal(0);
            }
            return skuEntity.getPrice().multiply(item.getCount()); // 实时小计
        }).reduce((a, b) -> a.add(b)).get();
        if (currentTotalPrice.compareTo(totalPrice) != 0) { // 不相等
            throw new OrderException("页面已过期！");
        }

        // TODO: 限购件数

        // 3.验库存并锁定库存（保证原子性）
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
        if (!CollectionUtils.isEmpty(skuLockVos)) {//锁定失败 解锁, 抛出异常
            throw new OrderException(JSON.toJSONString(skuLockVos));
        }

        // 4.创建订单
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long userId = userInfo.getUserId();
        try {
            this.omsClient.saveOrder(submitVo, userId);
            this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE", "order.ttl", orderToken);//定时关单
        } catch (Exception e) {
            e.printStackTrace();
            // 如果订单创建失败，发送消息给wms立马解锁库存，给oms标记为无效订单
            this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE", "order.fail", orderToken);
            throw new OrderException("服务器内部错误！");
        }

        // 5.异步删除购物车中对应的记录（MQ）
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
