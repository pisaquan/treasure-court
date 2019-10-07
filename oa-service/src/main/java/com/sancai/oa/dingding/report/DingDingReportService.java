package com.sancai.oa.dingding.report;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.sancai.oa.core.exception.EnumSystemError;
import com.sancai.oa.core.exception.OaException;
import com.sancai.oa.dingding.DingDingBase;
import com.sancai.oa.signin.exception.EnumSigninError;
import com.taobao.api.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 钉钉日报签到接口公共类
 * @author fans
 */
@Service
@Slf4j
public class DingDingReportService extends DingDingBase {


    /**
     * 钉钉取token的接口地址
     */
    @Value("${dingding.token-url}")
    private String getTokenUrl;
    /**获取企业的日志模板**/
    @Value("${dingding.listTemplate-url}")
     String listTemplateUrl;
    /**获取子部门列表**/
    @Value("${dingding.listDepartment-url}")
    private String  listDepartmentUrl;
    /**获取子部门ID列表**/
    @Value("${dingding.listDepartmentIds-url}")
    private String listDepartmentIdsUrl;
    /**获取部门用户userid列表**/
    @Value("${dingding.listGetDeptMember-url}")
    private String  listGetDeptMemberUrl;
    /**根据获取用户签到记录列表**/
    @Value("${dingding.listCheckinRecordMember-url}")
    private String  listCheckinRecordMemberUrl;
    /**根据部门获取用户签到记录列表**/
    @Value("${dingding.listCheckinRecordDepartment-url}")
    private String  listCheckinRecordDepartmentUrl;
    /**获取用户信息**/
    @Value("${dingding.getuser-url}")
    private String memberInfoUrl;
    /**获取日志统计数据**/
    @Value("${dingding.reportStatistics-url}")
    private String  reportStatisticsUrl;
    /**获取用户日志数据**/
    @Value("${dingding.reportList-url}")
    private String  reportListUrl;
    /** 获取应用的可见范围接口地址*/
    @Value("${dingding.visibleScopes-url}")
    private String visibleScopesUrl;
    /** 获取部门详情*/
    @Value("${dingding.departmentGet-url}")
    private String departmentGetUrl;



    /**
     * 获取企业的所有模板
     * @param userId 可选，不传表示获取企业的所有模板
     * @param companyId 公司id
     * @return  List<Map<String, Object>>
     */

    public  List<OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo> reportTemplate (String userId , String companyId){
        Long offset = 0L;
        List<OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo>  finalResultList = new ArrayList<>();
        while (offset != null){
            OapiReportTemplateListbyuseridRequest req = new OapiReportTemplateListbyuseridRequest();
            req.setUserid(userId);
            req.setOffset(offset);
            req.setSize(100L);
            OapiReportTemplateListbyuseridResponse rsp = (OapiReportTemplateListbyuseridResponse)request(listTemplateUrl,companyId,req);
            OapiReportTemplateListbyuseridResponse.HomePageReportTemplateVo templateVo = rsp.getResult();
            List<OapiReportTemplateListbyuseridResponse.ReportTemplateTopVo> templateList = templateVo.getTemplateList();
            if(templateList==null||templateList.size()==0){
                break;
            }
            finalResultList.addAll(templateList);
            if(templateVo.getNextCursor()!=null&&templateVo.getNextCursor()>0){
                offset = templateVo.getNextCursor();
            }else{
                offset = null;
            }
        }
        return finalResultList;
    }


    /**
     * 获取用户日志数据
     * @param userId 需要查询的用户列表，最大列表长度：10（如"1226682231742708,zhansan"）
     * @param templateName 模板名称
     * @param pageNo 分页查询的游标，最开始可以传0，然后以1、2依次递增
     * @return  List<Map<String, Object>>
     */
    public OapiReportListResponse.PageVo reportList(String userId , long pageNo , String companyId ,String templateName,long intervalTimeStart,long intervalTimeEnd){
        OapiReportListRequest request = new OapiReportListRequest();
        request.setStartTime(intervalTimeStart);
        request.setEndTime(intervalTimeEnd);
        request.setSize(100L);
        request.setCursor(pageNo);
        request.setUserid(userId);
        request.setTemplateName(templateName);
        OapiReportListResponse response = (OapiReportListResponse )request(reportListUrl,companyId,request);
        if(response.getErrcode() == 0){
            OapiReportListResponse.PageVo pageVo = response.getResult();
            return pageVo;
        }
        return null;
    }


    /**
     * 获取子部门ID列表(只返回部门id列表，如 [ 2,3,4,5 ])
     */

    public   List<Long> allDepartmentIds(String companyId ,String deptId) {
        OapiDepartmentListIdsRequest request = new OapiDepartmentListIdsRequest();
        request.setId(deptId);
        request.setHttpMethod("GET");
        OapiDepartmentListIdsResponse  response = (OapiDepartmentListIdsResponse)request(listDepartmentIdsUrl,companyId,request);
        List<Long> idList =response.getSubDeptIdList();
        if(idList!=null&&idList.size()>0){
            return idList;
        }
        return null;
    }
    /**
     * 获取部门用户userid列表
     */
    public List<String>  allUserIdByDeptId(String deptId , String companyId) {
        OapiUserGetDeptMemberRequest req = new OapiUserGetDeptMemberRequest();
        req.setDeptId(deptId);
        req.setHttpMethod("GET");
            OapiUserGetDeptMemberResponse  response = (OapiUserGetDeptMemberResponse )request(listGetDeptMemberUrl,companyId,req);
            List<String> userList =response.getUserIds();
            if(userList!=null&&userList.size()>0){
                return userList;
            }
        return null;
    }

    /**
     * 根据用户获取签到记录列表
     * @param useridList 需要查询的用户列表，最大列表长度：10（如"1226682231742708,zhansan"）
     * @param size 分页
     * @param pageNo 分页查询的游标，最开始可以传0，然后以1、2依次递增
     * size 分页查询的每页大小，最大100
     * @return  List<Map<String, Object>>
     */

    public  OapiCheckinRecordGetResponse.PageResult checkinUser(String useridList , long pageNo , long size ,String companyId,long intervalTimeStart ,long intervalTimeEnd){
        OapiCheckinRecordGetRequest request = new OapiCheckinRecordGetRequest();
        request.setStartTime(intervalTimeStart);
        request.setEndTime(intervalTimeEnd);
        request.setSize(size);
        request.setCursor(pageNo);
        request.setUseridList(useridList);
        OapiCheckinRecordGetResponse  response = (OapiCheckinRecordGetResponse )request(listCheckinRecordMemberUrl,companyId,request);
        if(response.getErrcode() == 0){
            OapiCheckinRecordGetResponse.PageResult pageResult =  response.getResult();
            if(pageResult!=null){
                return pageResult;
            }
        }
        return null;
    }
    /**
     * 根据部门获取用户签到记录
     *@param departmentId 部门id
     * @param pageNo 分页查询的游标，最开始可以传0，然后以1、2依次递增
     * @param size 分页查询的每页大小，最大100
     * @return  List<Map<String, Object>>
     */

    public Map<String , List<OapiCheckinRecordResponse.Data>> checkinUserByDepartmentId(String departmentId , long pageNo , long size , String companyId, long intervalTimeStart , long intervalTimeEnd){
        OapiCheckinRecordRequest  request = new OapiCheckinRecordRequest();
        request.setDepartmentId(departmentId);
        request.setStartTime(intervalTimeStart);
        request.setEndTime(intervalTimeEnd);
        request.setOffset(pageNo);
        request.setOrder("asc");
        request.setSize(size);
        request.setHttpMethod("GET");
        OapiCheckinRecordResponse  response;
        Map<String, List<OapiCheckinRecordResponse.Data>> map = new HashMap<>(16);
        map.put("data", null);
        try {
             response = (OapiCheckinRecordResponse)request(listCheckinRecordDepartmentUrl,companyId,request);
            if( response.getErrcode() == 0 && response.getData().size()>0){
                List<OapiCheckinRecordResponse.Data> dataList = response.getData();
                map.put("data", dataList);
                return map;
            }
        } catch (Exception e) {
            /** 目前最多获取1000人以内的签到数据，如果所传部门ID及其子部门下的user超过1000，会报错 errcode":40009,"errmsg":"不合法的部门id*/
           String code = e.getMessage().substring(0, e.getMessage().indexOf(":"));
           if(String.valueOf(EnumSigninError.ILLEGAL_DEPARTMENT_ID.getCode()).equals(code)){
               return null;
           }
            throw new OaException(EnumSystemError.DINGDING_ERROR);
        }
        return map;
    }

    /**
     * 获取用户信息
     * @param userId 用户id
     * @param companyId 公司id
     * @return Map
     * @throws ApiException 钉钉异常
     */
    public OapiUserGetResponse userinfoByUserId(String userId , String companyId){
        OapiUserGetRequest request = new OapiUserGetRequest();
        request.setUserid(userId);
        request.setHttpMethod("GET");
        OapiUserGetResponse  response;
        try {
            response = (OapiUserGetResponse)request(memberInfoUrl,companyId,request);
        } catch (Exception e) {
            log.warn("钉钉获取用户信息异常，查表获取用户信息");
            return null;
        }
        if(response.getErrcode() == 0) {
            return response;
        }
        return null;
    }

    /**
     * 效验AccessToken与agent_id
     * @param appKey 应用K
     * @param appSecret 应用秘钥
     * @param agentId 应用ID
     * @return boolean
     */
    public boolean checkAppKeySecretAndAgentId(String appKey,String appSecret,Long agentId) throws  ApiException {

            DingTalkClient client = new DefaultDingTalkClient(getTokenUrl);
            OapiGettokenRequest request = new OapiGettokenRequest();
            request.setAppkey(appKey);
            request.setAppsecret(appSecret);
            request.setHttpMethod("GET");
            OapiGettokenResponse response = client.execute(request);
            if(response.getErrcode() != 0){
                return false;
            }
            String token = response.getAccessToken();
            DingTalkClient  client2 = new DefaultDingTalkClient(visibleScopesUrl);
            OapiMicroappVisibleScopesRequest req = new OapiMicroappVisibleScopesRequest();
            req.setAgentId(agentId);
            OapiMicroappVisibleScopesResponse response2 = client2.execute(req, token);
            if(response2.getErrcode() !=0 ){
                return false;
            }
            return true;


    }
    /**
     * 获取部门详情
     * @param deptId 部门id
     * @return Map
     * @throws ApiException 钉钉异常
     */
    public OapiDepartmentGetResponse  deptInfoById(String deptId ,String companyId){
        OapiDepartmentGetRequest request = new OapiDepartmentGetRequest();
        request.setId(deptId);
        request.setHttpMethod("GET");
        OapiDepartmentGetResponse   response = (OapiDepartmentGetResponse)request(departmentGetUrl,companyId,request);
        if(response.getErrcode() == 0) {
            return response;
        }
        return null;
    }



}
