package com.offerpilot.manager;

import com.offerpilot.constant.PostCacheConstant;
import com.offerpilot.model.entity.Post;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostCacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public Post getPostFromCache(Long postId) {
        Object value = redisTemplate.opsForValue().get(PostCacheConstant.POST_DETAIL_PREFIX + postId);
        return value instanceof Post ? (Post) value : null;
    }

    public void setPostCache(Long postId, Post post) {
        long ttl = PostCacheConstant.POST_DETAIL_TTL_MINUTES + ThreadLocalRandom.current().nextLong(1, 11);
        redisTemplate.opsForValue().set(PostCacheConstant.POST_DETAIL_PREFIX + postId, post, ttl, TimeUnit.MINUTES);
        redisTemplate.delete(PostCacheConstant.POST_NULL_PREFIX + postId);
    }

    public boolean hasNullCache(Long postId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PostCacheConstant.POST_NULL_PREFIX + postId));
    }

    public void setNullCache(Long postId) {
        redisTemplate.opsForValue().set(PostCacheConstant.POST_NULL_PREFIX + postId, "1",
                PostCacheConstant.POST_NULL_TTL_MINUTES, TimeUnit.MINUTES);
    }

    public void deletePostCache(Long postId) {
        redisTemplate.delete(PostCacheConstant.POST_DETAIL_PREFIX + postId);
        redisTemplate.delete(PostCacheConstant.POST_NULL_PREFIX + postId);
    }
}
