package com.offerpilot.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("mq_message_log")
public class MqMessageLog implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String messageId;
    private String messageType;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
