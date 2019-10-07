package com.sancai.oa.signin.service;

/**
 * <p>
 * 发送外出签到确认通知 服务类
 * </p>
 *
 * @author fans
 * @since 2019-09-06
 */
public interface ISendOutingSigninTaskService{
    /**
     * 生成外出签到确认数据，发送外出签到确认通知
     * @param taskInstanceId
     * @param companyId
     * @return
     */
    boolean sendOutingSignin(String taskInstanceId, String companyId) ;
}
