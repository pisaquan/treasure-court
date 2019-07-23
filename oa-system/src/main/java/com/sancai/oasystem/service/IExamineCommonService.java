//package com.sancai.oasystem.service;
//
//import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
//import com.sancai.oasystem.bean.DdExamineInstanceVO;
//import com.sancai.oasystem.bean.enums.ExamineTypeEnum;
//
//import java.util.List;
//
///**
// * <p>
// * 审批 公共服务类
// * </p>
// * @author pisaquan
// * @since 2019/7/22 16:07
// */
//public interface IExamineCommonService {
//
//    /**
//     * 获取审批表单实体数据
//     * @param group 组
//     * @return
//     */
//    List<DdExamineInstanceVO> getDingTalkExamineData(String group);
//
//    /**
//     * 根据appkey和appsecret获取AccessToken
//     * @param appkey 应用的唯一标识key
//     * @param appsecret 应用的密钥
//     * @return
//     */
//    String getAccessToken(String appkey, String appsecret);
//
//    /**
//     *
//     * @param group
//     * @param companyId
//     * @return
//     */
//    String selectProcessCode(String group, String companyId);
//
//    /**
//     * 根据审批实例id获取审批实例详情
//     * @param ExamineInstanceId
//     * @param accessToken
//     * @return
//     */
//    OapiProcessinstanceGetResponse.ProcessInstanceTopVo ExamineInstanceGetById(String ExamineInstanceId, String accessToken);
//}
