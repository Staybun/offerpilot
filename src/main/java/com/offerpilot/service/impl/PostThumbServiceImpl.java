package com.offerpilot.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offerpilot.common.ErrorCode;
import com.offerpilot.exception.BusinessException;
import com.offerpilot.mapper.PostThumbMapper;
import com.offerpilot.model.entity.Post;
import com.offerpilot.model.entity.PostThumb;
import com.offerpilot.model.entity.User;
import com.offerpilot.mq.constant.PostActionTypeConstant;
import com.offerpilot.mq.dto.PostActionMessage;
import com.offerpilot.mq.producer.PostActionProducer;
import com.offerpilot.service.PostService;
import com.offerpilot.service.PostThumbService;
import javax.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 帖子点赞服务实现
 *
 */
@Service
public class PostThumbServiceImpl extends ServiceImpl<PostThumbMapper, PostThumb>
        implements PostThumbService {

    @Resource
    private PostService postService;
    @Resource
    private PostActionProducer postActionProducer;

    /**
     * 点赞
     *
     * @param postId
     * @param loginUser
     * @return
     */
    @Override
    public int doPostThumb(long postId, User loginUser) {
        // 判断实体是否存在，根据类别获取实体
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已点赞
        long userId = loginUser.getId();
        // 每个用户串行点赞
        // 锁必须要包裹住事务方法
        PostThumbService postThumbService = (PostThumbService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return postThumbService.doPostThumbInner(userId, postId);
        }
    }

    /**
     * 封装了事务的方法
     *
     * @param userId
     * @param postId
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int doPostThumbInner(long userId, long postId) {
        PostThumb postThumb = new PostThumb();
        postThumb.setUserId(userId);
        postThumb.setPostId(postId);
        QueryWrapper<PostThumb> thumbQueryWrapper = new QueryWrapper<>(postThumb);
        PostThumb oldPostThumb = this.getOne(thumbQueryWrapper);
        boolean result;
        int actionValue;
        // 已点赞
        if (oldPostThumb != null) {
            result = this.remove(thumbQueryWrapper);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            actionValue = -1;
        } else {
            result = this.save(postThumb);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            actionValue = 1;
        }
        registerSendAfterCommit(postId, userId, actionValue);
        return actionValue;
    }

    private void registerSendAfterCommit(long postId, long userId, int actionValue) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                PostActionMessage message = new PostActionMessage();
                message.setMessageId(IdUtil.fastSimpleUUID());
                message.setPostId(postId);
                message.setUserId(userId);
                message.setActionType(PostActionTypeConstant.THUMB);
                message.setActionValue(actionValue);
                message.setEventTime(System.currentTimeMillis());
                postActionProducer.sendPostActionMessage(message);
            }
        });
    }

}



