package com.sancai.oa.log.service;

import com.sancai.oa.log.entity.OperationLog;

public interface ISysLogService {
    void insertUserLog(OperationLog syslog);

    void insertAdminLog(OperationLog syslog);
}
