package com.offerpilot.mq.service;

import com.offerpilot.mq.constant.PostActionTypeConstant;
import com.offerpilot.mq.dto.PostActionMessage;
import com.offerpilot.service.MqMessageLogService;
import com.offerpilot.service.PostService;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostActionMessageHandler {
    @Resource
    private MqMessageLogService mqMessageLogService;
    @Resource
    private PostService postService;

    @Transactional(rollbackFor = Exception.class)
    public boolean handle(PostActionMessage message) {
        if (!mqMessageLogService.tryRecord(message.getMessageId(), message.getActionType())) {
            return false;
        }
        String column;
        if (PostActionTypeConstant.THUMB.equals(message.getActionType())) {
            column = "thumbNum";
        } else if (PostActionTypeConstant.FAVOUR.equals(message.getActionType())) {
            column = "favourNum";
        } else {
            throw new IllegalArgumentException("未知帖子行为类型");
        }
        int delta = message.getActionValue();
        boolean updated = postService.update().eq("id", message.getPostId())
                .setSql(column + " = GREATEST(" + column + " + (" + delta + "), 0)").update();
        if (!updated) {
            throw new IllegalStateException("帖子统计更新失败");
        }
        return true;
    }
}
