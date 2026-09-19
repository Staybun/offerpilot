package com.offerpilot.config;

import com.offerpilot.constant.PostCacheConstant;
import com.offerpilot.model.entity.Post;
import com.offerpilot.service.PostService;
import java.util.List;
import javax.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PostBloomFilterInitRunner implements CommandLineRunner {

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private PostService postService;

    @Override
    public void run(String... args) {
        RBloomFilter<Long> filter = redissonClient.getBloomFilter(PostCacheConstant.POST_BLOOM_FILTER);
        filter.tryInit(100000L, 0.01);
        List<Post> posts = postService.lambdaQuery().select(Post::getId).list();
        posts.forEach(post -> filter.add(post.getId()));
    }
}
