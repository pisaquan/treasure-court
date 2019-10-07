package com.sancai.oa.dingding.clockin;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiAttendanceListRequest;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiAttendanceListResponse;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.sancai.oa.clockin.entity.ClockinPoint;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author chenm
 * @create 2019/7/26 13:44
 */
@Slf4j
public class Main {

    private static List<String> userIds = new ArrayList<String>();
    static{

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

    public List<ClockinPoint> getNotSignedCheckPoints(String userId, long start, long end){
        return null;
    }

    public static void main(String[] args) {


        try {
//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/checkin/record");
//            OapiCheckinRecordRequest request = new OapiCheckinRecordRequest();
//            request.setDepartmentId("118050111");
//            request.setStartTime(1559370647000L);
//            request.setEndTime(1561876247000L);
//            request.setOffset(0L);
//            request.setOrder("asc");
//            request.setSize(100L);
//            request.setHttpMethod("GET");
//            OapiCheckinRecordResponse response = null;
//            response = client.execute(request, getToken());
//            List<OapiCheckinRecordResponse.Data> datas = response.getData();
//            for (OapiCheckinRecordResponse.Data data : datas) {
//                System.out.println(data.getUserId()+"-"+data.getName());
//            }


//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/get");
//            OapiUserGetRequest request = new OapiUserGetRequest();
//            request.setUserid("283723342836231475");
//            request.setHttpMethod("GET");
//            OapiUserGetResponse response = client.execute(request, getToken("xian"));
//            System.out.println(response.getName());


//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/getuserinfo");
//            OapiUserGetuserinfoRequest request = new OapiUserGetuserinfoRequest();
//            request.setCode("71e4575cf03631f6b6b43e6a5ebd8f12");
//            request.setHttpMethod("GET");
//            OapiUserGetuserinfoResponse response = client.execute(request, getToken("xian"));
//            String userId = response.getUserid();
//            System.out.println(userId);


//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/smartwork/hrm/employee/listdimission");
//            OapiSmartworkHrmEmployeeListdimissionRequest req = new OapiSmartworkHrmEmployeeListdimissionRequest();
//            req.setUseridList("192424062721543496");
//            OapiSmartworkHrmEmployeeListdimissionResponse response = client.execute(req ,  getToken());
//            System.out.println(response.getBody());


                //考勤
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/attendance/list");
            OapiAttendanceListRequest request = new OapiAttendanceListRequest();
            request.setWorkDateFrom("2019-09-15 00:00:00");
            request.setWorkDateTo("2019-09-15 23:00:00");
            request.setUserIdList(Arrays.asList("161802016635591582"));
            request.setOffset(0L);
            request.setLimit(50L);
            OapiAttendanceListResponse response = client.execute(request,getToken(Company.XIAN));
            System.out.println(response.getBody());
            for (OapiAttendanceListResponse.Recordresult arg : response.getRecordresult()) {
                if(arg.getGroupId() == 1L || arg.getGroupId() == -1L){
                    //未在考勤组中的打卡不抓取
                    System.out.println("未在考勤组中=== user:"+arg.getUserId()+"-group:"+arg.getGroupId()+"-"+arg.getTimeResult()+"-"+arg.getLocationResult()+"==="+arg.getBaseCheckTime()+"--"+arg.getUserCheckTime());
                    continue;
                }
                System.out.println("user:"+arg.getUserId()+"-group:"+arg.getGroupId()+"-"+arg.getTimeResult()+"-"+arg.getLocationResult()+"==="+arg.getBaseCheckTime()+"--"+arg.getUserCheckTime());
            }

//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/user/getuserinfo");
//            OapiUserGetuserinfoRequest request = new OapiUserGetuserinfoRequest();
//            request.setCode("3adb77e80d2235c0b0d4efe4c3c3402c");
//            request.setHttpMethod("GET");
//            OapiUserGetuserinfoResponse response = client.execute(request,getToken("xian"));
//            String userId = response.getUserid();
//            System.out.println(userId);

            //待办
//            List<OapiWorkrecordAddRequest.FormItemVo> item = new ArrayList();
//            OapiWorkrecordAddRequest.FormItemVo fi = new OapiWorkrecordAddRequest.FormItemVo();
//            fi.setTitle("testtitle");
//            fi.setContent("testcontent");
//            fi.setContent("testcontent");
//            item.add(fi);
//
//            OapiWorkrecordAddRequest req = new OapiWorkrecordAddRequest();
//            req.setUserid("05555018501218758");
//            req.setCreateTime(System.currentTimeMillis());
//            req.setTitle("title");
//            req.setUrl("http://www.baidu.com");
//            req.setFormItemList(item);
//
//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/workrecord/add");
//            OapiWorkrecordAddResponse response = client.execute(req,getToken("xian"));
//            if(response.getErrcode() == 0){
//                System.out.println(response.getRecordId());
//            }

            //离职信息
//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/smartwork/hrm/employee/listdimission");
//            OapiSmartworkHrmEmployeeListdimissionRequest req = new OapiSmartworkHrmEmployeeListdimissionRequest();
//            req.setUseridList("1963636567777433");
//            OapiSmartworkHrmEmployeeListdimissionResponse response = client.execute(req,getToken("xian"));
//            System.out.println(response.getBody());

            //通知
//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/message/corpconversation/asyncsend_v2");
//
//            OapiMessageCorpconversationAsyncsendV2Request request = new OapiMessageCorpconversationAsyncsendV2Request();
//            request.setUseridList("05555018501218758");
//            request.setAgentId(282575691L);
//            request.setToAllUser(false);
//
//            OapiMessageCorpconversationAsyncsendV2Request.Msg msg = new OapiMessageCorpconversationAsyncsendV2Request.Msg();
//            msg.setMsgtype("link");
//            msg.setLink(new OapiMessageCorpconversationAsyncsendV2Request.Link());
//            msg.getLink().setTitle("test");
//            msg.getLink().setText("test");
//            msg.getLink().setMessageUrl("http://www.baidu.com");
//            request.setMsg(msg);
//            OapiMessageCorpconversationAsyncsendV2Response response = client.execute(request,getToken("xian"));
//
//            System.out.println(response.getTaskId()+"-"+response.getErrmsg());


            //离职信息
//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/smartwork/hrm/employee/listdimission");
//            OapiSmartworkHrmEmployeeListdimissionRequest req = new OapiSmartworkHrmEmployeeListdimissionRequest();
//            req.setUseridList("290860634833078787");
//            OapiSmartworkHrmEmployeeListdimissionResponse response = client.execute(req , getToken(""));
//            System.out.println(response.getBody());

//            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/listids");
//            OapiProcessinstanceListidsRequest req = new OapiProcessinstanceListidsRequest();
//            req.setProcessCode("PROC-1D1CBA16-7923-408E-A595-3A0B8AB448A8");
//            req.setStartTime(1564588800000L);
//            req.setEndTime(1565107200000L);
//            req.setSize(20L);
//            req.setCursor(0L);
////            req.setUseridList("1563525840663938,15633577787169047");
//            OapiProcessinstanceListidsResponse response = client.execute(req, getToken("keji"));
//
//            for(String id : response.getResult().getList()){
//                DingTalkClient client2 = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/processinstance/get");
//                OapiProcessinstanceGetRequest request = new OapiProcessinstanceGetRequest();
//                request.setProcessInstanceId(id);
//                OapiProcessinstanceGetResponse response2 = client2.execute(request,getToken("keji"));
//                System.out.println(response2.getBody());
//                System.out.println("----------------------------------");
//            }






        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public enum Company{
        XIAN,KEJI,CESHI
    }
    private static String getToken(Company company) {

        DefaultDingTalkClient client = null;
        try {
            client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
            OapiGettokenRequest request = new OapiGettokenRequest();

            if(Company.XIAN.equals(company)){
                //西安
                request.setAppkey("dingp7h5vpizgefpnggh");
                request.setAppsecret("N_B9exWICWyQPmUUeA1hwHKUMXuIX8wtqVr55ZFyBwufD9XOyyvNB8u_q5G6OXIS");
            }else if(Company.KEJI.equals(company)){
                //科技
                request.setAppkey("dingvdldxxm5nwjgkc5q");
                request.setAppsecret("kJaXMg4Ro5w30yVeDoR804p5L0W2KKp7TdNkhr0-q38dS_Sc94vUDcafUqLV1USc");
            }else if(Company.CESHI.equals(company)){
                //测试架构
                request.setAppkey("ding9o0naudivlawtwnr");
                request.setAppsecret("77-wv3L1ox73BTgSLVx5IKoCX03Zw1Bc_9XfiJHBl7A8pmflTwywjnyz3e6W49ly");
            }


            request.setHttpMethod("GET");
            OapiGettokenResponse response = client.execute(request);

            return response.getAccessToken();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
