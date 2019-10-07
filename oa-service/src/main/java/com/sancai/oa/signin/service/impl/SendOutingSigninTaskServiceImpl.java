package com.sancai.oa.signin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.company.mapper.CompanyMapper;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.quartz.util.TaskMessage;
import com.sancai.oa.signin.entity.enums.SigninTypeEnum;
import com.sancai.oa.signin.service.ISendOutingSigninTaskService;
import com.sancai.oa.signin.service.ISigninRecordService;
import com.sancai.oa.signinconfirm.entity.SigninConfirm;
import com.sancai.oa.signinconfirm.mapper.SigninConfirmMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 发送外出签到确认通知 服务实现类
 * </p>
 *
 * @author fans
 * @since 2019-09-06
 */
@Service
public class SendOutingSigninTaskServiceImpl implements ISendOutingSigninTaskService {

    @Autowired
    private CompanyMapper companyMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SigninConfirmMapper signinConfirmMapper;
    @Autowired
    private ISigninRecordService signinRecordService;
    /**
     * 生成外出签到确认数据，发送外出签到确认通知
     * @param taskInstanceId
     * @param companyId
     * @return
     */
    @Override
    public boolean sendOutingSignin(String taskInstanceId, String companyId) {
        //生成外出签到确认数据
        boolean sendSuccess =  signinRecordService.sendOutingSignin(taskInstanceId,companyId);
        if(sendSuccess){
            //发送外出签到确认通知
            QueryWrapper<SigninConfirm> signinConfirmQueryWrapper = new QueryWrapper<>();
            signinConfirmQueryWrapper.lambda().eq(SigninConfirm::getTaskInstanceId,taskInstanceId);
            signinConfirmQueryWrapper.lambda().eq(SigninConfirm::getDeleted,0);
            signinConfirmQueryWrapper.lambda().eq(SigninConfirm::getStatus, SigninTypeEnum.UNCOMPLETED.getKey());
            signinConfirmQueryWrapper.lambda().isNotNull(SigninConfirm::getConfirmUserId);
            List<SigninConfirm> signinConfirmList = signinConfirmMapper.selectList(signinConfirmQueryWrapper);
            if(signinConfirmList == null || signinConfirmList.size() == 0){
                return false;
            }
            Company companyVO = companyMapper.selectOne(new QueryWrapper<Company>().lambda().eq(Company::getId, companyId).and(u -> u.eq(Company::getDeleted, 0)));
            if(companyVO == null){
                return false;
            }
            //公司应用id放入缓存，用于消息撤回
            String agentIdKey = taskInstanceId + "recallNotifyCompanyAgentId";
            redisUtil.set(agentIdKey , companyVO.getAgentId());

            signinConfirmList.stream().forEach(SigninConfirm -> {
                signinRecordService.sendToNotifAndBacklog(companyId, SigninConfirm.getConfirmUserId(), SigninConfirm, SigninConfirm.getDay() ,taskInstanceId);
            });
            String key = taskInstanceId + "recallNotify";
            redisUtil.del(key);
            redisUtil.del(agentIdKey);
            TaskMessage.finishMessage(taskInstanceId);
        }
        return sendSuccess;
    }
}
