package com.learn.gmall.wms.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        this.rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack){
                // 直接重发  记录到数据库  记录日志
                log.error("消息没有到达交换机。原因：{}", cause);
            }
        });
        this.rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.error("消息没有到达队列。交换机：{}，路由键：{}，消息内容：{}，回调状态码：{}，回调文本：{}",
                    exchange, routingKey, new String(message.getBody()), replyCode, replyText);
        });
    }

    /**
     * 业务交换机：ORDER.EXCHANGE
     */

    /**
     * 延时队列：STOCK.TTL.QUEUE
     */
    @Bean
    public Queue ttlQueue(){
        return QueueBuilder.durable("STOCK.TTL.QUEUE").ttl(100000)
                .deadLetterExchange("ORDER.EXCHANGE").deadLetterRoutingKey("stock.unlock").build();
    }

    /**
     * 把延时队列绑定到业务交换机：stock.ttl
     */
    @Bean
    public Binding ttlBinding(){
        return new Binding("STOCK.TTL.QUEUE", Binding.DestinationType.QUEUE, "ORDER.EXCHANGE",
                "stock.ttl", null);
    }

}