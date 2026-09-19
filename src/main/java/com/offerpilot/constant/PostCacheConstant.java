package com.offerpilot.constant;

public interface PostCacheConstant {
    String POST_DETAIL_PREFIX = "offerpilot:post:detail:";
    String POST_NULL_PREFIX = "offerpilot:post:null:";
    String POST_BLOOM_FILTER = "offerpilot:post:bloom";
    long POST_DETAIL_TTL_MINUTES = 30L;
    long POST_NULL_TTL_MINUTES = 2L;
}
