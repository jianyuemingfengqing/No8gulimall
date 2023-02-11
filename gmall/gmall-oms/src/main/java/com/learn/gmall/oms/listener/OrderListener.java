package com.learn.gmall.oms.listener;

import com.learn.gmall.oms.mapper.OrderMapper;
import com.rabbitmq.client.Channel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OrderListener {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("OMS.PAY.QUEUE"),
            exchange = @Exchange(value = "ORDER.EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"order.pay"}
    ))
    public void payOrder(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        if (this.orderMapper.updateStatus(orderToken, 0, 1) == 1) {
            // 发送消息给wms减库存
            this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE", "stock.minus", orderToken);
            // TODO: 发送消息给ums，给用户加积分
        } else {
            // 退款
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("OMS.DISABLE.QUEUE"),
            exchange = @Exchange(value = "ORDER.EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "true"),
            key = {"order.fail"}
    ))
    public void disableOrder(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        this.orderMapper.updateStatus(orderToken, 0, 5);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @RabbitListener(queues = "ORDER.DEAD.QUEUE")
    public void closeOrder(String orderToken, Channel channel, Message message) throws IOException {
        if (StringUtils.isBlank(orderToken)){
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;
        }

        // 执行关单操作，如果关单成功，发送消息给wms解锁库存
        if (this.orderMapper.updateStatus(orderToken, 0, 4) == 1) {
            this.rabbitTemplate.convertAndSend("ORDER.EXCHANGE", "stock.unlock", orderToken);
        }

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}
