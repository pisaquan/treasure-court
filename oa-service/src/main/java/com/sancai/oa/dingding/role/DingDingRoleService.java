package com.sancai.oa.dingding.role;

import com.dingtalk.api.request.OapiRoleListRequest;
import com.dingtalk.api.request.OapiRoleSimplelistRequest;
import com.dingtalk.api.response.OapiRoleListResponse;
import com.dingtalk.api.response.OapiRoleSimplelistResponse;
import com.sancai.oa.dingding.DingDingBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 *  钉钉用户角色相关
 * @Author fans
 * @create 2019/8/9 15:08
 */
@Service
@Slf4j
public class DingDingRoleService extends DingDingBase {

    /**获取用户角色列表**/
    @Value("${dingding.getrolelist-url}")
    private String  getrolelistUrl;

    /**获取角色下的员工列表**/
    @Value("${dingding.rolesimplelist-url}")
    private String  rolesimplelistUrl;





    /**
     * 获取角色列表
     * Size 分页大小，默认值：20，最大值200
     * Offset 分页偏移，默认值：0
     * @return
     */
    public  List<OapiRoleListResponse.OpenRoleGroup>  getRoleList(String companyId){
        boolean isNotWhile = true;
        long offset = 0L;
        List<OapiRoleListResponse.OpenRoleGroup>  finalResultList = new ArrayList<>();
        while (isNotWhile) {
            OapiRoleListRequest request = new OapiRoleListRequest();
            request.setOffset(offset);
            request.setSize(200L);
            OapiRoleListResponse response = (OapiRoleListResponse) request(getrolelistUrl, companyId, request);
             if(response.getErrcode() == 0){
                 OapiRoleListResponse.PageVo pageVo =  response.getResult();
                 if(pageVo!=null){
                     List<OapiRoleListResponse.OpenRoleGroup> openRoleGroups = pageVo.getList();
                     if(openRoleGroups!=null&&openRoleGroups.size()>0){
                         finalResultList.addAll(openRoleGroups);
                     }else {
                         isNotWhile = false;
                     }
                     //下页有数据
                     if(pageVo.getHasMore()){
                         offset = pageVo.getNextCursor();
                     }else{
                         isNotWhile = false;
                     }
                 }
             }
        }
        return finalResultList;
    }
    /**
     * 获取角色下的员工列表
     * role_id 角色ID
     * Size 分页大小，默认值：20，最大值200
     * Offset 分页偏移，默认值：0
     * @return
     */
    public  List<OapiRoleSimplelistResponse.OpenEmpSimple>  getUserListByRoleId(String companyId, long roleId){
        boolean isNotWhile = true;
        long offset = 0L;
        List<OapiRoleSimplelistResponse.OpenEmpSimple>  finalResultList = new ArrayList<>();
        while (isNotWhile) {
            OapiRoleSimplelistRequest request = new OapiRoleSimplelistRequest();
            request.setRoleId(roleId);
            request.setOffset(offset);
            request.setSize(200L);
            OapiRoleSimplelistResponse response = (OapiRoleSimplelistResponse) request(rolesimplelistUrl, companyId, request);
            if(response.getErrcode() == 0){
                OapiRoleSimplelistResponse.PageVo pageVo =  response.getResult();
                if(pageVo!=null){
                    List<OapiRoleSimplelistResponse.OpenEmpSimple> openEmpSimpleList = pageVo.getList();
                    if(openEmpSimpleList!=null&&openEmpSimpleList.size()>0){
                        finalResultList.addAll(openEmpSimpleList);
                    }else {
                        isNotWhile = false;
                    }
                    //下页有数据
                    if(pageVo.getHasMore()){
                        offset = pageVo.getNextCursor();
                    }else{
                        isNotWhile = false;
                    }
                }
            }
        }
        return finalResultList;
    }

    /**
     * 获取公司行政人事下人事专员角色的用户列表
     * @param companyId
     * @return
     */
    public  List<OapiRoleSimplelistResponse.OpenEmpSimple> getUserListByHrSpecialist (String companyId){
        List<OapiRoleListResponse.OpenRoleGroup> openRoleGroupList = getRoleList(companyId);
        if(openRoleGroupList == null||openRoleGroupList.size() == 0){
            return null;
        }
        for (OapiRoleListResponse.OpenRoleGroup  openRoleGroup : openRoleGroupList){
            if(openRoleGroup.getName().equals(HrTypeEnum.HR_GROUP.getValue())){
                List<OapiRoleListResponse.OpenRole> openRoles = openRoleGroup.getRoles();
                if(openRoles==null||openRoles.size()==0){
                    return null;
                }
                for(OapiRoleListResponse.OpenRole openRole :openRoles){
                    if(openRole.getName().equals(HrTypeEnum.HR_NAME.getValue())){
                        List<OapiRoleSimplelistResponse.OpenEmpSimple>  openEmpSimpleList = getUserListByRoleId(companyId,openRole.getId());
                        return openEmpSimpleList;
                    }
                }
            }
        }
        return null;
    }



}
