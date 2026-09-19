package com.offerpilot.job;

import com.offerpilot.constant.PostCacheConstant;
import com.offerpilot.model.entity.Post;
import com.offerpilot.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 帖子 BloomFilter 定时重建任务
 */
@Component
@Slf4j
public class PostBloomFilterRebuildJob {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private PostService postService;

    /**
     * 每天凌晨 2 点重建帖子 BloomFilter
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void rebuildPostBloomFilter() {
        log.info("开始重建帖子 BloomFilter");

        RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(PostCacheConstant.POST_BLOOM_FILTER);

        bloomFilter.delete();
        bloomFilter.tryInit(100000L, 0.01);

        List<Post> postList = postService.lambdaQuery()
                .select(Post::getId)
                .list();

        for (Post post : postList) {
            bloomFilter.add(post.getId());
        }

        log.info("帖子 BloomFilter 重建完成，数量 = {}", postList.size());

        // 示例输出：
        // 开始重建帖子 BloomFilter
        // 帖子 BloomFilter 重建完成，数量 = 328
    }
}