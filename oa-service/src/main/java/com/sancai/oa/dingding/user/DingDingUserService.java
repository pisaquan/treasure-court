package com.sancai.oa.dingding.user;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.*;
import com.dingtalk.api.response.*;
import com.sancai.oa.clockin.service.ClockinService;
import com.sancai.oa.core.cache.LocalCache;
import com.sancai.oa.dingding.DingDingBase;
import com.sancai.oa.dingding.clockin.DingDingClockinService;
import com.sancai.oa.dingding.department.DingDingDepartmentService;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  钉钉用户相关
 * @Author chenm
 * @create 2019/8/1 15:08
 */
@Service
@Slf4j
public class DingDingUserService extends DingDingBase {
    /**获取部门用户userid列表**/
    @Value("${dingding.listGetDeptMember-url}")
    private String  listGetDeptMemberUrl;
    /**获取员工的离职信息**/
    @Value("${dingding.userOfflineById-url}")
    private String  userOfflineByIdUrl;
    /**查询员工考勤组信息**/
    @Value("${dingding.getusergroup-url}")
    private String  getUserGroupUrl;

    /**
     *  获取用户信息
     */
    @Value("${dingding.getuser-url}")
    private String  getUserUrl;

    /**
     *  根据登录授权码获取userId
     */
    @Value("${dingding.getUserIdByCode-url}")
    private String getUserIdByCodeUrl;

    /**
     * 获取公司下所有在职员工信息
     */
    @Value("${dingding.userOnline-url}")
    private String userOnlineUrl;
    /**
     * 获取公司下所有离职员工信息
     */
    @Value("${dingding.userOffline-url}")
    private String userOfflineUrl;

    @Autowired
    private DingDingDepartmentService dingDingDepartmentService;

    @Autowired
    private DingDingUserService dingDingUserService;

    @Autowired
    private DingDingClockinService dingDingClockinService;

    @Autowired
    private ClockinService clockinService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private LocalCache localCache;

    private static Set<String> allUserIdSet = new HashSet<>();
    private static Set<Long> userCompletedDep = new HashSet<>();

    public static void addUserIds(Long deptId,List<String> userIdListByDep){
        allUserIdSet.addAll(userIdListByDep);
        userCompletedDep.add(deptId);
    }


    /**
     * 获取部门用户userid列表
     * @return
     */
    @Async
    public  List<String>  allUserIdByDeptId(Long deptId,String companyId){
        OapiUserGetDeptMemberRequest req = new OapiUserGetDeptMemberRequest();
        req.setDeptId(deptId.toString());
        req.setHttpMethod("GET");

        try {
            OapiUserGetDeptMemberResponse response = (OapiUserGetDeptMemberResponse) request(listGetDeptMemberUrl,companyId,req);
            return response.getUserIds();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

    }
    /**
     * 获取离职员工数据
     *
     * @param companyId
     * @return
     */
    public List<String> getOfflineUserByCompanyId(String companyId,int batch,Long page,boolean isEnd) {

        List<String> list = new ArrayList<String>();
        Long nextCursor = (batch-1)*page*50;
        OapiSmartworkHrmEmployeeQuerydimissionRequest req = new OapiSmartworkHrmEmployeeQuerydimissionRequest();
        req.setSize(50L);
        // 最后一个执行完成
        if(isEnd){
            while (nextCursor != null) {
                req.setOffset(nextCursor);
                OapiSmartworkHrmEmployeeQuerydimissionResponse response = (OapiSmartworkHrmEmployeeQuerydimissionResponse) request(userOfflineUrl, companyId, req);
                response.getResult();
                nextCursor = response.getResult().getNextCursor();
                list.addAll(response.getResult().getDataList());
            }
        }else{
            //中间的执行到指定页数退出
            int count =0;
            while (nextCursor != null && count < page ) {
                count++;
                req.setOffset(nextCursor);
                OapiSmartworkHrmEmployeeQuerydimissionResponse response = (OapiSmartworkHrmEmployeeQuerydimissionResponse) request(userOfflineUrl, companyId, req);
                response.getResult();
                nextCursor = response.getResult().getNextCursor();
                list.addAll(response.getResult().getDataList());
            }
        }


        return list;
    }

    /**
     * 取用户姓名
     * @param companyId
     * @param userId
     * @return
     */
    public String getUsetName(String companyId,String userId){
        String key = "user_name_"+userId;
        String name = (String) localCache.getValue(key);
        if(name !=  null){
            return name;
        }

        OapiUserGetRequest request = new OapiUserGetRequest();
        request.setUserid(userId);
        request.setHttpMethod("GET");
        OapiUserGetResponse response = (OapiUserGetResponse) request(getUserUrl,companyId,request);
        name = response.getName();
        localCache.putValue(key,name,3600 * 24);
        return name;
    }

    /**
     * 获取在职员工记录
     *
     * @param companyId
     * @param status
     * @return
     */
    public List<String> getOnlineUserByCompanyIdAndStatus(String companyId, String status) {
        List<String> list = new ArrayList<String>();
        Long nextCursor = 0L;
        OapiSmartworkHrmEmployeeQueryonjobRequest req = new OapiSmartworkHrmEmployeeQueryonjobRequest();
        req.setStatusList(status);
        req.setSize(20L);
        while (nextCursor != null) {
            req.setOffset(nextCursor);
            OapiSmartworkHrmEmployeeQueryonjobResponse response = (OapiSmartworkHrmEmployeeQueryonjobResponse) request(userOnlineUrl, companyId, req);
            response.getResult();
            nextCursor = response.getResult().getNextCursor();
            list.addAll(response.getResult().getDataList());
        }
        return list;
    }




    /**
     * 调用钉钉接口获取用户详情
     *
     * @param id
     * @return
     */
    public User getUser(String companyId, String id) {
        User user = new User();
        OapiUserGetRequest request = new OapiUserGetRequest();
        request.setUserid(id);
        request.setHttpMethod("GET");
        OapiUserGetResponse response = (OapiUserGetResponse) request(getUserUrl, companyId, request);

        user.setId(UUIDS.getID());
        user.setName(response.getName().trim().replace(" ",""));
        user.setCreateTime(System.currentTimeMillis());
        user.setDeleted(0);
        user.setMobile(response.getMobile());
        user.setUserId(response.getUserid());
        user.setCompanyId(companyId);
        return user;
    }

    /**
     * 根据钉钉登录授权码取用户id
     * @param companyId
     * @param code
     * @return
     */
    public String getUsetId(String companyId,String code){
        OapiUserGetuserinfoRequest request = new OapiUserGetuserinfoRequest();
        request.setCode(code);

        OapiUserGetuserinfoResponse response = (OapiUserGetuserinfoResponse)request(getUserIdByCodeUrl,companyId, request);
        return response.getUserid();
    }

    /**
     * 批量查询员工的离职信息
     * @return
     */
    public  List<OapiSmartworkHrmEmployeeListdimissionResponse.EmpDimissionInfoVo>  userOfflineById(String userIds, String companyId){
        OapiSmartworkHrmEmployeeListdimissionRequest req = new OapiSmartworkHrmEmployeeListdimissionRequest();
        req.setUseridList(userIds);
        OapiSmartworkHrmEmployeeListdimissionResponse response = (OapiSmartworkHrmEmployeeListdimissionResponse ) request(userOfflineByIdUrl,companyId,req);
        return response.getResult();
    }

    /**
     * 查询员工考勤组信息
     * @param companyId
     * @param userId
     * @return
     */
    public List<OapiAttendanceGetusergroupResponse.AtClassVo> getUserGroup (String companyId , String userId){
        OapiAttendanceGetusergroupRequest request = new OapiAttendanceGetusergroupRequest();
        request.setUserid(userId);
        OapiAttendanceGetusergroupResponse response = (OapiAttendanceGetusergroupResponse ) request(getUserGroupUrl,companyId,request);
        return response.getResult().getClasses();
    }



}
