package com.offerpilot.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offerpilot.mapper.PostFavourMapper;
import com.offerpilot.mapper.PostThumbMapper;
import com.offerpilot.model.entity.Post;
import com.offerpilot.model.entity.PostFavour;
import com.offerpilot.model.entity.PostThumb;
import com.offerpilot.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 帖子统计字段定时校准任务
 */
@Component
@Slf4j
public class PostStatisticSyncJob {

    @Resource
    private PostService postService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    /**
     * 每天凌晨 3 点，以关系表为准校准点赞数和收藏数
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void syncPostStatistic() {
        log.info("开始执行帖子统计字段定时校准任务");

        Map<Long, Long> postIdThumbCountMap = countThumbByPostId();
        Map<Long, Long> postIdFavourCountMap = countFavourByPostId();

        List<Post> postList = postService.lambdaQuery()
                .select(Post::getId)
                .list();

        for (Post post : postList) {
            Long postId = post.getId();

            Long thumbCount = postIdThumbCountMap.getOrDefault(postId, 0L);
            Long favourCount = postIdFavourCountMap.getOrDefault(postId, 0L);

            postService.update()
                    .eq("id", postId)
                    .set("thumbNum", thumbCount)
                    .set("favourNum", favourCount)
                    .update();
        }

        log.info("帖子统计字段定时校准任务完成，校准帖子数量 = {}", postList.size());

        // 示例输出：
        // 开始执行帖子统计字段定时校准任务
        // 帖子统计字段定时校准任务完成，校准帖子数量 = 328
    }

    private Map<Long, Long> countThumbByPostId() {
        QueryWrapper<PostThumb> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("postId", "count(*) as count")
                .groupBy("postId");

        List<Map<String, Object>> mapList = postThumbMapper.selectMaps(queryWrapper);

        Map<Long, Long> resultMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            Long postId = toLong(map.get("postId"));
            Long count = toLong(map.get("count"));
            resultMap.put(postId, count);
        }
        return resultMap;
    }

    private Map<Long, Long> countFavourByPostId() {
        QueryWrapper<PostFavour> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("postId", "count(*) as count")
                .groupBy("postId");

        List<Map<String, Object>> mapList = postFavourMapper.selectMaps(queryWrapper);

        Map<Long, Long> resultMap = new HashMap<>();
        for (Map<String, Object> map : mapList) {
            Long postId = toLong(map.get("postId"));
            Long count = toLong(map.get("count"));
            resultMap.put(postId, count);
        }
        return resultMap;
    }

    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }
}