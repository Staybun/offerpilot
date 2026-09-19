package com.offerpilot.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offerpilot.common.ErrorCode;
import com.offerpilot.exception.BusinessException;
import com.offerpilot.mapper.PostFavourMapper;
import com.offerpilot.model.entity.Post;
import com.offerpilot.model.entity.PostFavour;
import com.offerpilot.model.entity.User;
import com.offerpilot.service.PostFavourService;
import com.offerpilot.service.PostService;
import javax.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.IdUtil;
import com.offerpilot.mq.constant.PostActionTypeConstant;
import com.offerpilot.mq.dto.PostActionMessage;
import com.offerpilot.mq.producer.PostActionProducer;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
/**
 * 帖子收藏服务实现
 *
 */
@Service
public class PostFavourServiceImpl extends ServiceImpl<PostFavourMapper, PostFavour>
        implements PostFavourService {

    @Resource
    private PostService postService;
    @Resource
    private PostActionProducer postActionProducer;
    /**
     * 帖子收藏
     *
     * @param postId
     * @param loginUser
     * @return
     */
    @Override
    public int doPostFavour(long postId, User loginUser) {
        // 判断是否存在
        Post post = postService.getById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 是否已帖子收藏
        long userId = loginUser.getId();
        // 每个用户串行帖子收藏
        // 锁必须要包裹住事务方法
        PostFavourService postFavourService = (PostFavourService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return postFavourService.doPostFavourInner(userId, postId);
        }
    }

    @Override
    public Page<Post> listFavourPostByPage(IPage<Post> page, Wrapper<Post> queryWrapper, long favourUserId) {
        if (favourUserId <= 0) {
            return new Page<>();
        }
        return baseMapper.listFavourPostByPage(page, queryWrapper, favourUserId);
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

    public int doPostFavourInner(long userId, long postId) {
        PostFavour postFavour = new PostFavour();
        postFavour.setUserId(userId);
        postFavour.setPostId(postId);

        QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>(postFavour);
        PostFavour oldPostFavour = this.getOne(postFavourQueryWrapper);

        boolean result;
        int actionValue;

        // 已收藏：取消收藏
        if (oldPostFavour != null) {
            result = this.remove(postFavourQueryWrapper);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            actionValue = -1;
        } else {
            // 未收藏：收藏
            result = this.save(postFavour);
            if (!result) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            actionValue = 1;
        }

        // 事务提交后再发送 MQ
        registerSendPostActionMessageAfterCommit(postId, userId, PostActionTypeConstant.FAVOUR, actionValue);

        return actionValue;

    /*
    示例：
    用户未收藏 -> 插入 post_favour -> 事务提交后发 MQ：FAVOUR +1 -> 返回 1

    用户已收藏 -> 删除 post_favour -> 事务提交后发 MQ：FAVOUR -1 -> 返回 -1
    */
    }

    /**
     * 事务提交后发送帖子行为 MQ 消息
     */
    private void registerSendPostActionMessageAfterCommit(long postId, long userId, String actionType, int actionValue) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                PostActionMessage message = new PostActionMessage();
                message.setMessageId(IdUtil.fastSimpleUUID());
                message.setPostId(postId);
                message.setUserId(userId);
                message.setActionType(actionType);
                message.setActionValue(actionValue);
                message.setEventTime(System.currentTimeMillis());

                postActionProducer.sendPostActionMessage(message);
            }
        });
    }

}




