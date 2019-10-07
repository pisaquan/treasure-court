package com.sancai.oa.log.service.impl;


import com.sancai.oa.log.entity.OperationLog;
import com.sancai.oa.log.mapper.OperationLogMapper;
import com.sancai.oa.log.service.ISysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @Author fanjing
 * @create 2019/7/24 15:30
 */
@Service
public class SysLogServiceImpl implements ISysLogService {

    @Autowired
    OperationLogMapper tOperationLogMapper;

    /**
     * 插入日志记录实体类
     *
     * @param syslog 操作日志记录实体类
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void insertUserLog(OperationLog syslog) {
        tOperationLogMapper.insertUserLog(syslog);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void insertAdminLog(OperationLog syslog) {
        tOperationLogMapper.insertAdminLog(syslog);
    }
}

