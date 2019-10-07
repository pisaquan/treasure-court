package com.sancai.oa.clockin;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.request.OapiProcessinstanceListidsRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.dingtalk.api.response.OapiProcessinstanceListidsResponse;
import com.sancai.oa.Application;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.clockin.service.ClockinService;
import com.sancai.oa.clockin.service.IAttendanceRecordService;
import com.sancai.oa.clockin.service.IClockinRecordMergeService;
import com.sancai.oa.clockin.service.IClockinRecordService;
import com.sancai.oa.dingding.clockin.DingDingClockinService;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.examine.service.IExamineLeaveService;
import com.sancai.oa.signinconfirm.service.ISigninConfirmService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 打卡测试
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class TestClockinService {

    @Autowired
    private DingDingClockinService dingDingClockinService;

    @Autowired
    private DingDingUserService dingDingUserService;

    @Autowired
    IClockinRecordService clockinRecordService;

    @Autowired
    private ClockinService clockinService;

    @Autowired
    private ISigninConfirmService signinConfirmService;

    @Autowired
    private IExamineLeaveService examineLeaveService;

    @Autowired
    private IAttendanceRecordService iAttendanceRecordService;

    @Autowired
    private IClockinRecordMergeService clockinRecordMergeService;


    @Test
    /**
     * 取一个人一段时间内的缺卡的打卡点
     */
    public void testGetNotSignedCheckPoints(){
        String userId = "0144252036867756";
        String companyId = "C5BD8B0F1F4C40AEA94E7A4294EEC228";
        long start = 1564621199999L;
        long end =   1564632000001L;
        List<JSONObject> cps = clockinService.getNotSignedCheckPoints(companyId,userId,start,end);
        for (JSONObject cp : cps) {
            System.out.println(cp.getLong("baseCheckTime"));
        }

    }

    @Test
    public void signinConfirmService()throws Exception{
        DefaultDingTalkClient client1 = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
        OapiGettokenRequest request1 = new OapiGettokenRequest();
        request1.setAppkey("dingaevznrwvxcrxpwjq");
        request1.setAppsecret("UBJ6XzWAwAd12H5qhYBnwOjYsm0pq1MV1IIYVgDE_tNVxkBYiw3DGvCnV7y4-225");
        request1.setHttpMethod("GET");
        OapiGettokenResponse response1 = client1.execute(request1);

        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/listids");
        OapiProcessinstanceListidsRequest req = new OapiProcessinstanceListidsRequest();
        req.setProcessCode("PROC-EYCKIHSV-A2LTZIWGNYUSOGT4WQSU1-5VROUXEJ-2");
        req.setStartTime(1496678400000L);
        req.setEndTime(1496678400000L);

        OapiProcessinstanceListidsResponse response = client.execute(req, response1.getAccessToken());
        System.out.println(response);
    }

    @Test
    @Transactional
    public void deleteByTaskInstanceId(){
        QueryWrapper<ClockinRecord> wp = new QueryWrapper<>();
        wp.eq("task_instance_id","e4966475e6004ee5b581aa2d28cb4e23");
        wp.eq("deleted",0);
        Assert.assertTrue(clockinRecordService.list(wp).size()>0);

        clockinRecordService.deleteByTaskInstanceId("e4966475e6004ee5b581aa2d28cb4e23");

        Assert.assertTrue(clockinRecordService.list(wp).size()==0);


    }

    @Test
    public void testUpdate(){
        ClockinRecord cr  = clockinRecordService.getById("2d346c57b3f8428e910d41a24f0d5d07");

        cr.setContent(new Date().toLocaleString());
        cr.setModifyTime(System.currentTimeMillis());
        boolean r = clockinRecordService.updateById(cr);

        Assert.assertTrue(r);
    }

    @Test
    public void testClockinRecordMerge(){
        clockinRecordMergeService.consolidatedAttendanceData("C5BD8B0F1F4C40AEA94E7A4294EEC228","32fcd48e25fa4aba883d3cc440bde383");
    }

    @Test
    public void testStatisticAttendanceResult(){
        //iAttendanceRecordService.pageStatisticAttendanceResult("727052b0a78c430e90a510111658ff2b", "17f18638dff64ed098543a456f5590ac");
        //clockinRecordMergeService.consolidatedAttendanceData("727052b0a78c430e90a510111658ff2b", "b19f152cbde34605b47b0406339fcd8b");
        //examineLeaveService.pullLeaveExamineData("F4754EF7878141A5A52C833769064D1F","05b3a3da137c4f24b6194419919499e7");
        //try {
        //    clockinService.graspClockinRecord("e3ab3a4be2b7410490b0ef3c9e336dc6","727052b0a78c430e90a510111658ff2b");
        //}catch (Exception e){
        //}
        examineLeaveService.pullLeaveExamineData("C5BD8B0F1F4C40AEA94E7A4294EEC228","05b3a3da137c4f24b6194419919499e7");
    }

}
