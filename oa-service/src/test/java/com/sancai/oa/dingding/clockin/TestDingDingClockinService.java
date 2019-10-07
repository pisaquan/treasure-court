package com.sancai.oa.dingding.clockin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.dingtalk.api.response.OapiAttendanceListscheduleResponse;
import com.sancai.oa.Application;
import com.sancai.oa.clockin.entity.ClockinDepartment;
import com.sancai.oa.clockin.entity.ClockinPoint;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.clockin.mapper.ClockinDepartmentMapper;
import com.sancai.oa.clockin.service.ClockinService;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.service.IUserService;
import com.sancai.oa.utils.ListUtils;
import com.sancai.oa.utils.LocalDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * 考勤测试
 * @Author chenm
 * @create 2019/7/23 9:48
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class TestDingDingClockinService {

    @Autowired
    private DingDingClockinService dingDingClockinService;

    @Autowired
    private DingDingUserService dingDingUserService;

    @Autowired
    private ClockinService clockinService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ClockinDepartmentMapper clockinDepartmentMapper;

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
     * 排班
     */
    public void listscheduleTest(){

        try {
            List<OapiAttendanceListscheduleResponse.AtScheduleForTopVo> schedules = dingDingClockinService.cheduleList("1",new Date(),0L,100L);

            for(OapiAttendanceListscheduleResponse.AtScheduleForTopVo sc:schedules){
                log.error(sc.getPlanCheckTime().toGMTString());
            }
            log.error("success");
        } catch (Exception e) {
           log.error(e.getMessage());
        }

    }


    @Test
    /**
     * 打卡结果
     */
    public void attendanceListTest(){
        try {
            String companyId = "F4754EF7878141A5A52C833769064D1F";
            String month = "2019-08";
            LocalDateTime yesterday = LocalDateTime.now().plusDays(-2);
            long today = LocalDateTimeUtils.getDayStart(yesterday).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
            long tomorrow = LocalDateTimeUtils.getDayEnd(yesterday).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();

            List<String> userIds2 = new ArrayList<String>();
            //陈明
            userIds2.add("0144252036867756");
            Map<String, Map<Long,List<ClockinPoint>>> userClockinPointMap = dingDingClockinService.onedayClockinRecords(companyId,today,tomorrow,userIds2);
            for(Map.Entry<String,Map<Long,List<ClockinPoint>>> entry:userClockinPointMap.entrySet()){
                String userId = entry.getKey();

                Map<Long,List<ClockinPoint>> cpMap = entry.getValue();
                ClockinRecord cr  = clockinService.getUserMonthClockinRecord(companyId,userId,month);
                if(cr == null){
                    //第一天的数据
                    cr = new ClockinRecord();
                    cr.setId(UUIDS.getID());
                    cr.setUserId(userId);
                    cr.setMonth(month);
                    cr.setUserName(dingDingUserService.getUsetName(companyId,userId));
                    cr.setCompanyId(companyId);
                    cr.setCreateTime(LocalDateTime.now().toInstant(ZoneOffset.ofHours(8)).toEpochMilli());
                    cr.setDeleted(0);
                    cr.setTaskInstanceId("aaa");
                    JSONObject jo = (JSONObject) JSON.toJSON(cpMap);
                    cr.setContent(jo.toJSONString());
                    clockinService.save(cr);
                }else{
                    //追加
                    String content = cr.getContent();
                    Map<Long,List<ClockinPoint>> crpMap = JSON.parseObject(content,Map.class);
                    crpMap.putAll(cpMap);
                    JSONObject jo = (JSONObject) JSON.toJSON(crpMap);
                    cr.setContent(jo.toJSONString());
                    clockinService.updateById(cr);
                }
            }

            log.error("finish---");

        } catch (Exception e) {
            log.error(e.getMessage());
        }


    }

    @Test
    public void graspClockinRecordTest() throws Exception {
        List<TransactionStatus> transactionStatuses = Collections.synchronizedList(new ArrayList<TransactionStatus>());

        String companyId = "C5BD8B0F1F4C40AEA94E7A4294EEC228";
        String taskInstanceId = "112233";
        long start = LocalDateTimeUtils.convertTimeToLong("2019-08-14 00:00:00");
        long end = LocalDateTimeUtils.convertTimeToLong("2019-08-14 23:59:00");
        List<UserDTO> users = userService.listUser(companyId,0,0L,System.currentTimeMillis());
        System.out.println("总用户数："+users.size());

        //钉钉考勤一次只能取50个用户
        List<List<UserDTO>> userIdBatch = ListUtils.fixedGrouping(users,50);
        clockinService.graspClockinRecord(taskInstanceId,companyId,userIdBatch,start,end,true);

        log.error("finish---");
        boolean finish = true;
        while (finish){
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void deleteCd(){
        UpdateWrapper<ClockinDepartment> wrapper = new UpdateWrapper<ClockinDepartment>();
        wrapper.eq("clockin_record_id","b2fdbc2c33bb455bb08459eaa77b20a6");
        int row = clockinDepartmentMapper.delete(wrapper);
        System.out.println(row+"---");

    }


}