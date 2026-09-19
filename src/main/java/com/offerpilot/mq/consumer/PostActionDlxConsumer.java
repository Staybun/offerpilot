package com.offerpilot.mq.consumer;

import com.offerpilot.mq.constant.RabbitMqConstant;
import com.offerpilot.mq.dto.PostActionMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostActionDlxConsumer {
    @RabbitListener(queues = RabbitMqConstant.POST_ACTION_DLX_QUEUE)
    public void consume(PostActionMessage message) {
        log.error("帖子行为消息进入死信队列，等待人工或定时补偿，message={}", message);
    }
}
