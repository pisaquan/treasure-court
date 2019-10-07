package com.sancai.oa.department.service;

import com.dingtalk.api.response.OapiDepartmentListResponse;

import java.util.List;

/**
 * <p>
 * 分公司 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-26
 */
public interface IDepartmentQuartzService {
    /**
     * quartz 获取公司下的部门信息
     */
    public void departmentList(String companyid, String taskInstanceId) throws Exception ;
    /**
     * 获取公司下所有部门里的员工信息，并持久化到数据库中
     *
     * @param companyId
     * @param departmentList
     */
    public void updateUserinfoByDepartmentId(String companyId, List<OapiDepartmentListResponse.Department> departmentList, String taskInstanceId) throws Exception ;

}
