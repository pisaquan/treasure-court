package com.sancai.oa.log.mapper;

import com.sancai.oa.log.entity.OperationLog;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author fanjing
 * @since 2019-07-24
 */
@Repository
public interface OperationLogMapper {

    void insertUserLog(OperationLog syslog);

    void insertAdminLog(OperationLog syslog);
}
