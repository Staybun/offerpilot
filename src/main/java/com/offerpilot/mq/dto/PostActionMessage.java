package com.offerpilot.mq.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class PostActionMessage implements Serializable {
    private String messageId;
    private Long postId;
    private Long userId;
    private String actionType;
    private Integer actionValue;
    private Long eventTime;
}
