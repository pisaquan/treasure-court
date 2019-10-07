package com.sancai.oa.dingding.department;

import com.dingtalk.api.request.OapiDepartmentListRequest;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.sancai.oa.dingding.DingDingBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 钉钉部门相关接口
 * @Author chenm
 * @create 2019/8/1 15:09
 */
@Service
@Slf4j
public class DingDingDepartmentService extends DingDingBase {
    /**获取子部门列表**/
    @Value("${dingding.listDepartment-url}")
    private String  listDepartmentUrl;

    /**
     * 获取分公司下部门列表
     */
    @Value("${dingding.department-url}")
    private String departmentUrl;
    /**
     * 获取子部门列表（返回部门较详细数据，如名称，父部门id等）
     * @return
     */
    public List<OapiDepartmentListResponse.Department> allDepartment(String companyId){
        OapiDepartmentListRequest request = new OapiDepartmentListRequest ();
        request.setId("1");
        request.setFetchChild(true);
        request.setHttpMethod("GET");

        OapiDepartmentListResponse response = (OapiDepartmentListResponse) request(listDepartmentUrl,companyId,request);

        return response.getDepartment();
    }

    /**
     * 获取分公司下部门列表
     * @return
     */
    public List<OapiDepartmentListResponse.Department> departmentList(String companyId){
        OapiDepartmentListRequest request = new OapiDepartmentListRequest();
        request.setId("1");
        request.setHttpMethod("GET");
        OapiDepartmentListResponse response = (OapiDepartmentListResponse) request(departmentUrl, companyId, request);
        // 获取钉钉里departmentList
        List<OapiDepartmentListResponse.Department> departmentList = response.getDepartment();
        for(OapiDepartmentListResponse.Department department :departmentList){
            department.setName(department.getName().trim().replace(" ",""));
        }
        return departmentList;
    }


}
