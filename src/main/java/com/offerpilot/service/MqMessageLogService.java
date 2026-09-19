package com.offerpilot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.offerpilot.model.entity.MqMessageLog;

public interface MqMessageLogService extends IService<MqMessageLog> {
    boolean tryRecord(String messageId, String messageType);
}
