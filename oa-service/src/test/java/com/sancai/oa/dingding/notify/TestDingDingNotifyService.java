package com.sancai.oa.dingding.notify;


import com.sancai.oa.Application;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;


/**
 * 考勤测试
 * @Author chenm
 * @create 2019/7/23 9:48
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class TestDingDingNotifyService {

    @Autowired
    private DingDingNotifyService dingDingNotifyService;

    private static List<String> userIds = new ArrayList<String>();
    {

        //陈明
        userIds.add("05555018501218758");
        //范松松
        userIds.add("243653281433078787");
        //权雷雷
        userIds.add("6644331026640739");
        //王亚林
        userIds.add("0551112829075720");
        //邓佳艳
        userIds.add("283723342836231475");
        //樊晶
        userIds.add("0144252036867756");
    }

    @Test
    /**
     * 通知
     */
    public void sendToNotifyTest(){

        String companyId= "C5BD8B0F1F4C40AEA94E7A4294EEC228";
        String userId="05555018501218758";
        String title = "考勤通知 2019-08";
        String url = "http://www.baidu.com";

        OANotifyDTO notify = new OANotifyDTO(companyId, userId, url, title);
        notify.addParam("参数1","aaaa");
        notify.addParam("参数2","bbbb");
        notify.addParam("参数3","cccc");
        Long whetherSuccess = dingDingNotifyService.sendOAMessage(notify);
        log.error("发送结果:"+whetherSuccess);
    }
}

