package com.sancai.oa.dingding.examine;

import com.dingtalk.api.request.OapiProcessinstanceGetRequest;
import com.dingtalk.api.request.OapiProcessinstanceListidsRequest;
import com.dingtalk.api.request.OapiUserGetRequest;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.dingtalk.api.response.OapiProcessinstanceListidsResponse;
import com.sancai.oa.dingding.DingDingBase;
import com.sancai.oa.examine.entity.ExamineInstanceDTO;
import com.sancai.oa.examine.entity.enums.ExamineFormCompEnum;
import com.sancai.oa.utils.OaMapUtils;
import com.taobao.api.ApiException;
import com.taobao.api.TaobaoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pisaquan
 * @since 2019/7/27 10:23
 */
@Service
@Slf4j
public class DingDingExamineService extends DingDingBase{

    /**
     * 批量获取审批实例id
     */
    @Value("${dingding.examineinstanlistids-url}")
    private String examineInstIdListUrl;

    /**
     * 批获取审批实例详情
     */
    @Value("${dingding.examineinstance-url}")
    private String examineInstanceUrl;

    /**
     * 获取用户详情
     */
    @Value("${dingding.getuser-url}")
    private String getUserUrl;


    /**
     * 获取审批表单实体数据
     * @param companyId
     * @param processCode
     * @return
     */

    public Map<List<ExamineInstanceDTO>,Long> getDingTalkExamineData(Long nextCursor, String companyId, String processCode,long intervalTimeStart,long intervalTimeEnd) {
        OapiProcessinstanceListidsRequest req = new OapiProcessinstanceListidsRequest();
        req.setProcessCode(processCode);
        req.setStartTime(intervalTimeStart);
        req.setEndTime(intervalTimeEnd);
        //最多传20
        req.setSize(20L);
        req.setCursor(nextCursor);
        OapiProcessinstanceListidsResponse response = (OapiProcessinstanceListidsResponse)request(examineInstIdListUrl,companyId,req);
        if(response != null && response.getErrcode() == 0){
            OapiProcessinstanceListidsResponse.PageResult pageResult = response.getResult();
            List<String> idList = pageResult.getList();
            List<ExamineInstanceDTO> processInstanceVoList = new ArrayList<>();
            idList.stream().forEach(id -> {
                OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo = examineInstanceGetById(id,companyId);
                ExamineInstanceDTO ddExamineInstanceVO = new ExamineInstanceDTO();
                List<OapiProcessinstanceGetResponse.FormComponentValueVo> formComponentValueVoList = processInstanceTopVo.getFormComponentValues();
                formComponentValueVoList.stream().filter(ex ->
                        ExamineFormCompEnum.COMPANY.getValue().equals(ex.getName())).forEach(formComponentValueVo -> {
                    ddExamineInstanceVO.setCompany(formComponentValueVo.getValue());
                });
                ddExamineInstanceVO.setProcessInstanceId(id);
                ddExamineInstanceVO.setProcessCode(processCode);
                ddExamineInstanceVO.setCompanyId(companyId);
                ddExamineInstanceVO.setProcessInstanceTopVo(processInstanceTopVo);
                processInstanceVoList.add(ddExamineInstanceVO);
            });
            Map<List<ExamineInstanceDTO>,Long> resultMap = new HashMap<>(1);
            resultMap.put(processInstanceVoList,pageResult.getNextCursor());
            return resultMap;
        }
        return null;
    }

    /**
     * 根据审批实例id获取审批实例详情
     * @param ExamineInstanceId
     * @param companyId
     * @return
     */
    public OapiProcessinstanceGetResponse.ProcessInstanceTopVo examineInstanceGetById(String ExamineInstanceId,String companyId) {
        OapiProcessinstanceGetRequest request = new OapiProcessinstanceGetRequest();
        request.setProcessInstanceId(ExamineInstanceId);
        OapiProcessinstanceGetResponse response = (OapiProcessinstanceGetResponse)request(examineInstanceUrl,companyId,request);
        OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo = null;
        if(response != null && response.getErrcode() == 0){
            processInstanceTopVo = response.getProcessInstance();
        }
        return processInstanceTopVo;
    }

    /**
     * 获取用户信息
     * @return
     */
    public Map<String, Object> getUserInfoById(String userId , String companyId) {
        OapiUserGetRequest request = new OapiUserGetRequest();
        request.setUserid(userId);
        request.setHttpMethod("GET");
        TaobaoResponse response;
        try {
            response = request(getUserUrl,companyId,request);
        }catch (Exception e){
            log.warn("钉钉获取用户信息异常，查表获取用户信息");
            return null;
        }
        Map<String, Object> rsMap = OaMapUtils.stringToMap(response.getBody());
        if(!OaMapUtils.mapIsAnyBlank(rsMap,"errcode") && rsMap.get("errcode").toString().equals("0")){
            return rsMap;
        }
        return null;
    }
}
