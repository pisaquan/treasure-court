package com.sancai.oasystem.service;

import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.sancai.oasystem.bean.DdExamineInstanceVO;
import com.sancai.oasystem.bean.ExamineBaseVO;
import com.sancai.oasystem.bean.TExamineLeave;

import javax.print.DocFlavor;
import java.util.List;

/**
 * <p>
 * 审批 公共服务类
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
public interface IExamineCommonService {

    /**
     * 获取审批表单实体数据
     * @return 审批表单实体
     */
    List<DdExamineInstanceVO> pullDingTalkExamineData(String group);

    /**
     * 根据appkey和appsecret获取AccessToken
     * @param appkey  应用的唯一标识key
     * @param appsecret 应用的密钥
     * @return access_token
     */
    String getAccessToken(String appkey,String appsecret);

    /**
     * 根据group和companyId取审批模板详情
     * @param group 审批类型
     * @param companyId  子公司id
     * @return  审批模板标识
     */
    String selectProcessCode(String group,String companyId);

    /**
     * 根据审批实例id获取审批实例详情
     * @param ExamineInstanceId
     * @return
     */
    OapiProcessinstanceGetResponse.ProcessInstanceTopVo ExamineInstanceGetById(String ExamineInstanceId,String accessToken);
}
