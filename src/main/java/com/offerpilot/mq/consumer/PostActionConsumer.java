package com.offerpilot.mq.consumer;

import com.offerpilot.manager.PostCacheManager;
import com.offerpilot.mq.constant.RabbitMqConstant;
import com.offerpilot.mq.dto.PostActionMessage;
import com.offerpilot.mq.service.PostActionMessageHandler;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PostActionConsumer {
    @Resource
    private PostActionMessageHandler handler;
    @Resource
    private PostCacheManager postCacheManager;

    @RabbitListener(queues = RabbitMqConstant.POST_ACTION_QUEUE)
    public void consume(PostActionMessage message, Channel channel,
            org.springframework.amqp.core.Message amqpMessage) throws IOException {
        long tag = amqpMessage.getMessageProperties().getDeliveryTag();
        try {
            if (handler.handle(message) && message.getPostId() != null) {
                postCacheManager.deletePostCache(message.getPostId());
            }
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("消费帖子行为消息失败，message={}", message, e);
            channel.basicNack(tag, false, false);
        }
    }
}
