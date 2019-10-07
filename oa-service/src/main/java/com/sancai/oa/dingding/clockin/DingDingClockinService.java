package com.sancai.oa.dingding.clockin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.request.OapiAttendanceListRequest;
import com.dingtalk.api.request.OapiAttendanceListscheduleRequest;
import com.dingtalk.api.response.OapiAttendanceListResponse;
import com.dingtalk.api.response.OapiAttendanceListscheduleResponse;
import com.sancai.oa.clockin.entity.ClockinPoint;
import com.sancai.oa.clockin.enums.EnumDingDingAttendanceCheckType;
import com.sancai.oa.clockin.enums.EnumDingDingAttendanceLocationResult;
import com.sancai.oa.clockin.enums.EnumDingDingAttendanceTimeResult;
import com.sancai.oa.dingding.DingDingBase;
import com.sancai.oa.utils.LocalDateTimeUtils;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;


/**
 * 钉钉打卡相关的接口
 * @Author chenm
 * @create 2019/7/25 13:35
 */
@Service
@Slf4j
public class DingDingClockinService extends DingDingBase {

    /**
     * 钉钉取排班的接口地址
     */
    @Value("${dingding.listschedule-url}")
    private String listscheduleUrl;

    /**
     * 钉钉取打卡结果的接口地址
     */
    @Value("${dingding.attendance-url}")
    private String attendanceUrl;



    /**
     * 取所有排班班次
     * @param companyId
     * @return
     * @throws ApiException
     */
    public List<OapiAttendanceListscheduleResponse.AtScheduleForTopVo> cheduleList(String companyId,Date date,long offset,long size){
        OapiAttendanceListscheduleRequest request = new OapiAttendanceListscheduleRequest();
        request.setWorkDate(date);
        request.setOffset(offset);
        request.setSize(size);

        OapiAttendanceListscheduleResponse response = (OapiAttendanceListscheduleResponse) request(listscheduleUrl,companyId,request);

        return response.getResult().getSchedules();
    }


    /**
     *  按用户id取打卡结果，起始与结束工作日最多相隔7天，用户id不能超过50个
     * @param companyId
     * @param startTime
     * @param endTime
     * @param userIds
     * @return
     * @throws ApiException
     */
    public Map<String,Map<Long,List<ClockinPoint>>> onedayClockinRecords(String companyId,long startTime,long endTime,List<String> userIds) {
        Map<String,Map<Long,List<ClockinPoint>>> userClockinPointMap = new HashMap<String,Map<Long,List<ClockinPoint>>>();

        if(userIds == null || userIds.size() == 0){
            return userClockinPointMap;
        }
        //本批次考勤记录

        List<OapiAttendanceListResponse.Recordresult> records = attendanceList(companyId,startTime,endTime,userIds);
        if(records == null || records.size() == 0){
            return userClockinPointMap;
        }
        Map<String,List<OapiAttendanceListResponse.Recordresult>> userClockinMap = new HashMap<String,List<OapiAttendanceListResponse.Recordresult>>();

        for (OapiAttendanceListResponse.Recordresult record : records) {
            if(record.getGroupId() == 1L || record.getGroupId() == -1L){
                //未在考勤组中的打卡不抓取
                continue;
            }
            List<OapiAttendanceListResponse.Recordresult> userResult = userClockinMap.get(record.getUserId());
            if(userResult == null){
                userResult = new ArrayList<OapiAttendanceListResponse.Recordresult>();
            }
            log.error("test log === onedayClockinRecords companyId:"+companyId+",userId+"+record.getUserId()+" 考勤 record -- bt:"+record.getBaseCheckTime()+", ut:"+record.getUserCheckTime());
            userResult.add(record);
            userClockinMap.put(record.getUserId(),userResult);
        }
        //处理成一个用户的考勤一个map
        userClockinPointMap = buildUserClockinPointMap(userClockinMap);
        return userClockinPointMap;
    }


    /**
     * 处理成一个用户的考勤一个map
     * @param userClockinMap
     * @return
     */
    private  Map<String,Map<Long,List<ClockinPoint>>> buildUserClockinPointMap(Map<String,List<OapiAttendanceListResponse.Recordresult>> userClockinMap){
        Map<String,Map<Long,List<ClockinPoint>>> userClockinPointMap = new HashMap<String,Map<Long,List<ClockinPoint>>>();
        for(Map.Entry<String,List<OapiAttendanceListResponse.Recordresult>> entry:userClockinMap.entrySet()){
            String userId = entry.getKey();

            List<OapiAttendanceListResponse.Recordresult> userResult = entry.getValue();
            //按考勤时间排序
            Collections.sort(userResult,new Comparator<OapiAttendanceListResponse.Recordresult>() {
                @Override
                public int compare(OapiAttendanceListResponse.Recordresult o1, OapiAttendanceListResponse.Recordresult o2) {
                    return o1.getBaseCheckTime().compareTo(o2.getBaseCheckTime());
                }
            });

            Map<Long,List<ClockinPoint>> cpMap = new LinkedHashMap<Long,List<ClockinPoint>>();

            for (OapiAttendanceListResponse.Recordresult record : userResult) {
                EnumDingDingAttendanceTimeResult timeResult = EnumDingDingAttendanceTimeResult.valueOf(record.getTimeResult());
                EnumDingDingAttendanceLocationResult locationResult = EnumDingDingAttendanceLocationResult.valueOf(record.getLocationResult());
                EnumDingDingAttendanceCheckType checkType = EnumDingDingAttendanceCheckType.valueOf(record.getCheckType());

                Date workDate = record.getWorkDate();
                List<ClockinPoint> cps = cpMap.get(workDate.getTime());
                if(cps == null){
                    cps = new ArrayList<ClockinPoint>();
                }
                //打卡点
                ClockinPoint cp = new ClockinPoint(record.getId(),record.getBaseCheckTime(),record.getUserCheckTime(),checkType,locationResult,timeResult);
                cps.add(cp);
                cpMap.put(workDate.getTime(),cps);
            }
            JSONObject jo = (JSONObject) JSON.toJSON(cpMap);
            log.error("test log === buildUserClockinPointMap userId:"+userId+",cpMap:"+jo.toJSONString());

            userClockinPointMap.put(userId,cpMap);

        }

        return  userClockinPointMap;
    }


    /**
     *
     * @param companyId
     * @param workDateFrom 起始时间
     * @param workDateTo 结束时间，起始与结束工作日最多相隔7天
     * @param userIds 用户id列表，最多不能超过50个
     * @throws ApiException
     */
    public List<OapiAttendanceListResponse.Recordresult> attendanceList(String companyId, long workDateFrom, long workDateTo, List<String> userIds){
        List<OapiAttendanceListResponse.Recordresult> attendanceList = new ArrayList<>();
        long offset = 0L;
        long limit = 50L;
        while (true){
            OapiAttendanceListRequest request = new OapiAttendanceListRequest();
            String from = LocalDateTimeUtils.formatDateTime(workDateFrom);
            String to = LocalDateTimeUtils.formatDateTime(workDateTo);
            request.setWorkDateFrom(from);
            request.setWorkDateTo(to);
            request.setUserIdList(userIds);
            request.setOffset(offset);
            //最大不能超过50条
            request.setLimit(limit);
            OapiAttendanceListResponse response = (OapiAttendanceListResponse) request(attendanceUrl,companyId,request);

            List<OapiAttendanceListResponse.Recordresult> result = response.getRecordresult();
            if(result == null || result.size() == 0){
                break;
            }
            attendanceList.addAll(result);
            offset+=limit;
        }

        return attendanceList;


    }


}
