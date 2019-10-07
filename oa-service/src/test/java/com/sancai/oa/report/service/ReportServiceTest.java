package com.sancai.oa.report.service;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiMessageCorpconversationRecallRequest;
import com.dingtalk.api.request.OapiWorkrecordAddRequest;
import com.dingtalk.api.response.OapiMessageCorpconversationRecallResponse;
import com.sancai.oa.Application;
import com.sancai.oa.dingding.DingDingBase;
import com.sancai.oa.dingding.backlog.DingDingBacklogService;
import com.sancai.oa.dingding.backlog.SendBacklogDTO;
import com.sancai.oa.dingding.notify.DingDingNotifyService;
import com.sancai.oa.dingding.notify.LinkNotifyDTO;
import com.sancai.oa.dingding.notify.OANotifyDTO;
import com.sancai.oa.signin.service.ISigninRecordService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author chenm
 * @create 2019/7/23 9:48
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class ReportServiceTest extends DingDingBase {

    @Autowired
    private IReportRecordService itReportRecordService;
    @Autowired
    private ISigninRecordService tsigninRecordService;
    @Autowired
    IReportTemplateService itReportTemplateService;
    @Autowired
    DingDingNotifyService dingDingNotifyService;
    @Autowired
    DingDingBacklogService dingDingBacklogService;


    @Test
    public void importEveryDayReportData(){
        String companyId = "C5BD8B0F1F4C40AEA94E7A4294EEC228";
        String taskInstanceId = "A48512A940214327BD011A646F5E910D";
        boolean result = itReportRecordService.importEveryDayReportData(taskInstanceId,companyId);
        Assert.assertEquals(true, result);
        while(true){

        }
    }


    @Test
    @Transactional
    public void sendToNotify() throws Exception {
        String companyId= "C5BD8B0F1F4C40AEA94E7A4294EEC228";
        String userId="243653281433078787";
        String title = "测试考勤通知 2019-08";
        String url = "http://www.baidu.com";

        OANotifyDTO notify = new OANotifyDTO(companyId, userId, url, title);
        notify.addParam("参数1","aaaa");
        Long yon = dingDingNotifyService.sendOAMessage(notify);
        Assert.assertEquals(null, yon);
    }
    @Test
    @Transactional
    public void recallNotify() throws Exception {//刘鹏46407094511
        String companyId= "C5BD8B0F1F4C40AEA94E7A4294EEC228";
        Long msgTaskId = 46325025808L;
        Long agentId = 282575691L;
        boolean yon = dingDingNotifyService.recallNotify(agentId,msgTaskId,companyId);
        Assert.assertEquals(true, yon);
    }

    @Test
    @Transactional
    public void sendToWork() throws Exception {
        SendBacklogDTO sendBacklogDTO = new SendBacklogDTO();
        sendBacklogDTO.setManagerId("0551112829075720");
        sendBacklogDTO.setTitle("测试代办");
        sendBacklogDTO.setUrl("www.baidu.com");
        sendBacklogDTO.setCompanyId("C5BD8B0F1F4C40AEA94E7A4294EEC228");
        List<OapiWorkrecordAddRequest.FormItemVo> item = new ArrayList();
        OapiWorkrecordAddRequest.FormItemVo fi = new OapiWorkrecordAddRequest.FormItemVo();
        fi.setTitle("testtitle");
        fi.setContent("testcontent");
        fi.setContent("testcontent");
        item.add(fi);
        sendBacklogDTO.setFormItemList(item);
        String id = dingDingBacklogService.sendToBacklog(sendBacklogDTO);
        System.out.println("代办id：" + id);
    }

}
