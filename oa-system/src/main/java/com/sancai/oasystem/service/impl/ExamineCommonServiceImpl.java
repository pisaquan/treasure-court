package com.sancai.oasystem.service.impl;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiProcessinstanceGetRequest;
import com.dingtalk.api.request.OapiProcessinstanceListidsRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.dingtalk.api.response.OapiProcessinstanceListidsResponse;
import com.sancai.oasystem.bean.ExamineBaseVO;
import com.sancai.oasystem.bean.TExamineLeave;
import com.sancai.oasystem.bean.enums.ExamineTypeEnum;
import com.sancai.oasystem.service.IExamineCommonService;
import com.taobao.api.ApiException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * <p>
 * 审批 公共服务实现类
 * </p>
 *
 * @author pisaquan
 * @since 2019-07-18
 */
@Service
public class ExamineCommonServiceImpl implements IExamineCommonService {

    @Override
    public List<TExamineLeave> pullDingTalkExamineData(String group) {
        List<TExamineLeave> examineEntityList = new ArrayList<TExamineLeave>();
        //TODO 查询所有子公司信息
        //String processCode = selectProcessCode(group,"276194902");
        String processCode = "PROC-5011A4D8-C732-41D1-9E9E-94FD84F0D848";
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/listids");
        OapiProcessinstanceListidsRequest req = new OapiProcessinstanceListidsRequest();
        req.setProcessCode(processCode);
        Long startTime = TimeConversionUtil.getBeginDayOfYesterday().getTime();
        req.setStartTime(startTime);
        String accessToken = getAccessToken("ding9p7vzgvgrx3tvd6a","nGKknhri4XwBjcLxkZFacafx10_k67dUT6B09kJF50xLBrwb9AULlVQxcI4L0W3W");
        OapiProcessinstanceListidsResponse response = null;
        try {
            response = client.execute(req, accessToken);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        if(response != null && response.getErrcode() == 0){
            OapiProcessinstanceListidsResponse.PageResult pageResult = response.getResult();
            List<String> idList = pageResult.getList();
            idList.stream().forEach(id -> {
                OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo = ExamineInstanceGetById(id,accessToken);
                TExamineLeave examineBaseVO = new TExamineLeave();
                examineBaseVO.setId(UUIDS.getID());
                examineBaseVO.setUserId(processInstanceTopVo.getOriginatorUserid());
                examineBaseVO.setProcessCode(processCode);
                examineBaseVO.setProcessInstanceId(id);
                examineBaseVO.setProcessTitle(processInstanceTopVo.getTitle());
                examineBaseVO.setProcessCreateTime(processInstanceTopVo.getCreateTime().getTime());
                examineBaseVO.setProcessFinishTime(processInstanceTopVo.getFinishTime().getTime());
                examineBaseVO.setProcessStatus(processInstanceTopVo.getStatus());
                examineBaseVO.setProcessResult(processInstanceTopVo.getResult());
                examineBaseVO.setFormValueCompany("三彩科技");
                examineBaseVO.setFormValueSalary("带薪");
                examineBaseVO.setFormValueType("事假");
                examineBaseVO.setFormValueStart(1496678400000L);
                examineBaseVO.setFormValueFinish(1496678400000L);
                examineBaseVO.setFormValueDays(1);
                examineBaseVO.setFormValueReason("体检");
                examineBaseVO.setCreateTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
                examineBaseVO.setModifyTime(Instant.now().toEpochMilli());
                examineBaseVO.setDeleted(0);
                examineEntityList.add(examineBaseVO);
            });
        }
        return examineEntityList;
    }

    @Override
    public String getAccessToken(String appkey, String appsecret) {
        DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
        OapiGettokenRequest request = new OapiGettokenRequest();
        request.setAppkey(appkey);
        request.setAppsecret(appsecret);
        request.setHttpMethod("GET");
        OapiGettokenResponse response = null;
        try {
            response = client.execute(request);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        return response != null ? response.getAccessToken(): null;
    }

    @Override
    public String selectProcessCode(String group, String companyId) {
        //TODO 根据group和companyId取审批模板详情
        return null;
    }

    @Override
    public OapiProcessinstanceGetResponse.ProcessInstanceTopVo ExamineInstanceGetById(String ExamineInstanceId, String accessToken) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/get");
        OapiProcessinstanceGetRequest request = new OapiProcessinstanceGetRequest();
        request.setProcessInstanceId(ExamineInstanceId);
        OapiProcessinstanceGetResponse response = null;
        try {
            response = client.execute(request,accessToken);
        } catch (ApiException e) {
            e.printStackTrace();
        }
        OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo = null;
        if(response != null && response.getErrcode() == 0){
            processInstanceTopVo = response.getProcessInstance();
        }
        return processInstanceTopVo;
    }

}
