package com.learn.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.learn.gmall.wms.mapper.WareSkuMapper;
import com.learn.gmall.wms.vo.SkuLockVo;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;

@Component
public class StockListener {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private WareSkuMapper wareSkuMapper;
    private static final String KEY_PREFIX = "stock:info:";

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("STOCK.MINUS.QUEUE"),
            exchange = @Exchange(value = "ORDER.EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"stock.minus"}
    ))
    public void minus(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 1.根据orderToken查询锁定信息的缓存
        String skuLockVoJsons = this.redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);

        // 2.判断锁定信息的缓存是否为空
        if (StringUtils.isBlank(skuLockVoJsons)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        List<SkuLockVo> skuLockVos = JSON.parseArray(skuLockVoJsons, SkuLockVo.class);
        if (CollectionUtils.isEmpty(skuLockVos)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 3.遍历锁定信息的缓存，减库存
        skuLockVos.forEach(lockVo -> {
            this.wareSkuMapper.minus(lockVo.getWareSkuId(), lockVo.getCount());
        });

        // 4.删除锁定信息的缓存
        this.redisTemplate.delete(KEY_PREFIX + orderToken);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("STOCK.UNLOCK.QUEUE"),
            exchange = @Exchange(value = "ORDER.EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"order.fail", "stock.unlock"}
    ))
    public void unlock(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 1.根据orderToken查询锁定信息的缓存
        String skuLockVoJsons = this.redisTemplate.opsForValue().get(KEY_PREFIX + orderToken);

        // 2.判断锁定信息的缓存是否为空
        if (StringUtils.isBlank(skuLockVoJsons)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }
        List<SkuLockVo> skuLockVos = JSON.parseArray(skuLockVoJsons, SkuLockVo.class);
        if (CollectionUtils.isEmpty(skuLockVos)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 3.遍历锁定信息的缓存，解锁库存
        skuLockVos.forEach(lockVo -> {
            this.wareSkuMapper.unlock(lockVo.getWareSkuId(), lockVo.getCount());
        });

        // 4.删除锁定信息的缓存
        this.redisTemplate.delete(KEY_PREFIX + orderToken);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
