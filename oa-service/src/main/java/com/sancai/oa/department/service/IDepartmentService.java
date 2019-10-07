package com.sancai.oa.department.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.entity.DepartmentDTO;

import com.taobao.api.ApiException;

import java.util.List;

/**
 * <p>
 * 分公司 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-26
 */
public interface IDepartmentService {
    /**
     * quartz 获取公司下的部门信息
     * @param company_id  公司id
     */
    public DepartmentDTO getDepartmentByCompayid(String company_id);
    /**
     * 获取子部门信息
     *
     * @param root 根节点
     * @param departmentList  部门列表
     * @return
     */
    public DepartmentDTO findChildrenTreebyList(DepartmentDTO root , List<Department> departmentList);
    /**
     * quartz 获取公司下的部门列表信息
     * @param company_id  公司id
     */
    public List<Department> listDepartment(String company_id);

    /**
     *  获取部门下的子部门id集合信息
     * @param companyId
     * @param deptId
     * @return
     */
    public List<Long> listSubDepartment(String companyId,String deptId);

    /**
     * 遍历部门信息
     *
     * @param dept_id 部门id
     */
    public Department getDepartment(String dept_id);

    /**
     * 获取上级部门名称
     * @param companyId 公司id
     * @param deptId  部门id
     * @return
     */
    public String getSuperiorsDepartmentName(String companyId,String deptId);
}
