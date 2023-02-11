package com.learn.gmall.cart.listener;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.learn.gmall.cart.feign.GmallPmsClient;
import com.learn.gmall.cart.mapper.CartMapper;
import com.learn.gmall.cart.pojo.Cart;
import com.learn.gmall.common.bean.ResponseVo;
import com.learn.gmall.pms.entity.SkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class CartListener {
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private static final String PRICE_PREFIX = "cart:price:";
    private static final String KEY_PREFIX = "cart:info:";
    @Autowired
    private CartMapper cartMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("CART.DELETE.QUEUE"),
            exchange = @Exchange(value = "ORDER.EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"cart.delete"}
    ))
    public void deleteCart(Map<String, Object> msg, Channel channel, Message message) throws IOException {
        if (CollectionUtils.isEmpty(msg)) { //防止垃圾消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 获取消息中的userId和skuIds
        Long userId = Long.valueOf(msg.get("userId").toString());
        List<String> skuIds = JSON.parseArray(msg.get("skuIds").toString(), String.class);//用string template所以用string来接收
        if (userId == null || CollectionUtils.isEmpty(skuIds)) {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 删除购物车中对应的记录
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(KEY_PREFIX + userId);
        hashOps.delete(skuIds.toArray());
        this.cartMapper.delete(new UpdateWrapper<Cart>().eq("user_id", userId).in("sku_id", skuIds));

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("CART.PRICE.QUEUE"),
            exchange = @Exchange(value = "PMS.SPU.EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"item.update"}
    ))
    public void syncPrice(Long spuId, Channel channel, Message message) throws IOException {

        if (spuId == null) { //没有商品
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 根据spuId查询sku
        ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.list(spuId);
        List<SkuEntity> skuEntities = skuResponseVo.getData();
        if (CollectionUtils.isEmpty(skuEntities)) {//没有数据
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 遍历sku同步实时价格
        skuEntities.forEach(skuEntity -> {
            this.redisTemplate.opsForValue().setIfPresent(PRICE_PREFIX + skuEntity.getId(), skuEntity.getPrice().toString());
        });

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
