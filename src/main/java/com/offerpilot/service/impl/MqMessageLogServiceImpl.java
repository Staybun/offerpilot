package com.offerpilot.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offerpilot.mapper.MqMessageLogMapper;
import com.offerpilot.model.entity.MqMessageLog;
import com.offerpilot.service.MqMessageLogService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
public class MqMessageLogServiceImpl extends ServiceImpl<MqMessageLogMapper, MqMessageLog>
        implements MqMessageLogService {
    @Override
    public boolean tryRecord(String messageId, String messageType) {
        MqMessageLog log = new MqMessageLog();
        log.setMessageId(messageId);
        log.setMessageType(messageType);
        log.setStatus(1);
        try {
            return save(log);
        } catch (DuplicateKeyException e) {
            return false;
        }
    }
}
