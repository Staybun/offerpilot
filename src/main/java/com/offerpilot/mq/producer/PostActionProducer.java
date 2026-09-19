package com.offerpilot.mq.producer;

import com.offerpilot.mq.constant.RabbitMqConstant;
import com.offerpilot.mq.dto.PostActionMessage;
import javax.annotation.Resource;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostActionProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;
    public void sendPostActionMessage(PostActionMessage message) {
        rabbitTemplate.convertAndSend(RabbitMqConstant.POST_ACTION_EXCHANGE,
                RabbitMqConstant.POST_ACTION_ROUTING_KEY, message);
    }
}
