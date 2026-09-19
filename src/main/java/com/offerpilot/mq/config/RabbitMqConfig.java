package com.offerpilot.mq.config;

import com.offerpilot.mq.constant.RabbitMqConstant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    public DirectExchange postActionExchange() {
        return new DirectExchange(RabbitMqConstant.POST_ACTION_EXCHANGE, true, false);
    }
    @Bean
    public DirectExchange postActionDlxExchange() {
        return new DirectExchange(RabbitMqConstant.POST_ACTION_DLX_EXCHANGE, true, false);
    }
    @Bean
    public Queue postActionQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", RabbitMqConstant.POST_ACTION_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", RabbitMqConstant.POST_ACTION_DLX_ROUTING_KEY);
        return QueueBuilder.durable(RabbitMqConstant.POST_ACTION_QUEUE).withArguments(args).build();
    }
    @Bean
    public Queue postActionDlxQueue() {
        return QueueBuilder.durable(RabbitMqConstant.POST_ACTION_DLX_QUEUE).build();
    }
    @Bean
    public Binding postActionBinding() {
        return BindingBuilder.bind(postActionQueue()).to(postActionExchange())
                .with(RabbitMqConstant.POST_ACTION_ROUTING_KEY);
    }
    @Bean
    public Binding postActionDlxBinding() {
        return BindingBuilder.bind(postActionDlxQueue()).to(postActionDlxExchange())
                .with(RabbitMqConstant.POST_ACTION_DLX_ROUTING_KEY);
    }
}
