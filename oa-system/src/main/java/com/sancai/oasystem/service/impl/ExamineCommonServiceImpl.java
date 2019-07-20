package com.sancai.oasystem.service.impl;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiProcessinstanceGetRequest;
import com.dingtalk.api.request.OapiProcessinstanceListidsRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiProcessinstanceGetResponse;
import com.dingtalk.api.response.OapiProcessinstanceListidsResponse;
import com.sancai.oasystem.bean.DdExamineInstanceVO;
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
    public List<DdExamineInstanceVO> pullDingTalkExamineData(String group) {

        Long next_cursor = 0L;
        String accessToken = getAccessToken("ding9p7vzgvgrx3tvd6a","nGKknhri4XwBjcLxkZFacafx10_k67dUT6B09kJF50xLBrwb9AULlVQxcI4L0W3W");
        Long startTime = TimeConversionUtil.getBeginDayOfYesterday().getTime();

        List<DdExamineInstanceVO> processInstanceVoList = new ArrayList<>();
        //TODO 查询所有子公司信息
        //String processCode = selectProcessCode(group,"276194902");
        String processCode = "PROC-5011A4D8-C732-41D1-9E9E-94FD84F0D848";
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/listids");
        OapiProcessinstanceListidsRequest req = new OapiProcessinstanceListidsRequest();
        req.setProcessCode(processCode);
        req.setStartTime(startTime);
        req.setEndTime(Timestamp.valueOf(LocalDateTime.now()).getTime());
        req.setSize(1L);
        while(next_cursor != null){
            req.setCursor(next_cursor);
            OapiProcessinstanceListidsResponse response = null;
            try {
                response = client.execute(req, accessToken);
            } catch (ApiException e) {
                e.printStackTrace();
            }
            if(response != null && response.getErrcode() == 0){
                OapiProcessinstanceListidsResponse.PageResult pageResult = response.getResult();
                List<String> idList = pageResult.getList();
                next_cursor = pageResult.getNextCursor();
                idList.stream().forEach(id -> {
                    OapiProcessinstanceGetResponse.ProcessInstanceTopVo processInstanceTopVo = ExamineInstanceGetById(id,accessToken);

                    DdExamineInstanceVO ddExamineInstanceVO = new DdExamineInstanceVO();
                    ddExamineInstanceVO.setId(id);
                    ddExamineInstanceVO.setProcessCode(processCode);
                    ddExamineInstanceVO.setProcessInstanceTopVo(processInstanceTopVo);
                    processInstanceVoList.add(ddExamineInstanceVO);
                });
            }
        }
        return processInstanceVoList;
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
    public OapiProcessinstanceGetResponse.ProcessInstanceTopVo ExamineInstanceGetById(String ExamineInstanceId,String accessToken) {
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
