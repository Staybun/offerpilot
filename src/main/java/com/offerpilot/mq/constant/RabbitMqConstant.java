package com.offerpilot.mq.constant;

public interface RabbitMqConstant {
    String POST_ACTION_EXCHANGE = "offerpilot.post.action.exchange";
    String POST_ACTION_QUEUE = "offerpilot.post.action.queue";
    String POST_ACTION_ROUTING_KEY = "offerpilot.post.action";
    String POST_ACTION_DLX_EXCHANGE = "offerpilot.post.action.dlx.exchange";
    String POST_ACTION_DLX_QUEUE = "offerpilot.post.action.dlx.queue";
    String POST_ACTION_DLX_ROUTING_KEY = "offerpilot.post.action.dlx";
}
