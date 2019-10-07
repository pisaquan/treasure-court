package com.sancai.oa.department.controller;

import com.github.pagehelper.PageInfo;
import com.sancai.oa.core.ApiResponse;
import com.sancai.oa.core.version.ApiVersion;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.entity.DepartmentDTO;
import com.sancai.oa.department.exception.EnumDepartmentError;
import com.sancai.oa.department.exception.OaDepartmentlException;
import com.sancai.oa.department.service.IDepartmentQuartzService;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.signinconfirm.exception.EnumSigninConfirmError;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 部门控制层
 * @Author wangyl
 * @create 2019/7/25 09:14
 */

@ApiVersion(1)
@Slf4j
@RestController
@RequestMapping("{version}/department")
public class DepartmentController {
 
    @Autowired
    private IDepartmentService departmentServiceImpl;

    @Autowired
    private IDepartmentQuartzService departmentQuartzService;
    /**
     * 获取部门详情
     * @param dept_id 部门id
     * @return
     */
    @GetMapping("department_detail/{dept_id}")
    public ApiResponse getDepartment(@PathVariable String dept_id){
        Department department = new Department();
        if(StringUtils.isBlank(dept_id)){
            return ApiResponse.fail(EnumDepartmentError.DEPARTMENT_ID_EMPTY);
        }
        try {
            department = departmentServiceImpl.getDepartment(dept_id);
        } catch (Exception e) {
            throw new OaDepartmentlException(EnumDepartmentError.DEPARTMENT_NOT_FOUND);
        }

        return ApiResponse.success(department);
    }
    /**
     * 获取上级部门名称
     * @param map
     * @return
     */
    @PostMapping("department_superiors")
    public ApiResponse getSuperiorsDepartmentName(@RequestBody Map<String, Object> map) {

        String companyId = map.get("company_id")+"";
        String deptId =  map.get("dept_id")+"";
        String res = "";
        if(StringUtils.isBlank(deptId)){
            return ApiResponse.fail(EnumDepartmentError.DEPARTMENT_ID_EMPTY);
        }
        if(StringUtils.isBlank(companyId)){
            return ApiResponse.fail(EnumDepartmentError.COMPANY_ID_EMPTY);
        }
        try {
            res = departmentServiceImpl.getSuperiorsDepartmentName(companyId,deptId);
        } catch (Exception e) {
            throw new OaDepartmentlException(EnumDepartmentError.DEPARTMENT_NOT_FOUND);
        }
        return ApiResponse.success(res);
    }
    /**
     * 获取部门列表tree结构
     * @param company_id 公司ID
     * @return
     */
    @GetMapping("department_tree/{company_id}")
    public ApiResponse getDepartmentTree(@PathVariable String company_id){
        DepartmentDTO departmentDto = new DepartmentDTO();

        if(StringUtils.isBlank(company_id)){
            return ApiResponse.fail(EnumDepartmentError.COMPANY_ID_EMPTY);
        }
        try {
            departmentDto = departmentServiceImpl.getDepartmentByCompayid(company_id);
        } catch (Exception e) {
            throw new OaDepartmentlException(EnumDepartmentError.DEPARTMENT_TREE_NOT_FOUND);
        }

        return ApiResponse.success(departmentDto);
    }

    /**
     * 获取部门列表 list结构
     * @param company_id 部门ID
     * @return
     */
    @GetMapping("department_list/{company_id}")
    public ApiResponse listDepartment(@PathVariable String company_id){

        List<Department> result = new ArrayList<>();
        if(StringUtils.isBlank(company_id)){
            return ApiResponse.fail(EnumDepartmentError.COMPANY_ID_EMPTY);
        }
        try {
            result = departmentServiceImpl.listDepartment(company_id);
        } catch (Exception e) {
            throw new OaDepartmentlException(EnumDepartmentError.DEPARTMENT_LIST_NOT_FOUND);

        }

        if(result == null||result.size()==0){
            ApiResponse.success(new PageInfo<>( new ArrayList<>()));
        }

        //分页对象
        return ApiResponse.success(new PageInfo<>(result));
    }

    /**
     * 获取部门和用户信息并持久化到数据库中
     * @param companyId
     * @param taskInstanceId
     * @return
     */
    public ApiResponse getDepartmentAndUserinfo(@RequestParam("companyId") String companyId,@RequestParam("taskInstanceId") String taskInstanceId) throws Exception{
        if(StringUtils.isBlank(companyId)){
            return ApiResponse.fail(EnumDepartmentError.COMPANY_ID_EMPTY);
        }
        departmentQuartzService.departmentList(companyId,taskInstanceId);

        try {
            departmentQuartzService.departmentList(companyId,taskInstanceId);
        } catch (Exception e) {
            throw e;
        }

        return ApiResponse.success();
    }
}