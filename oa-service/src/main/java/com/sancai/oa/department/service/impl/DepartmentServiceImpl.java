package com.sancai.oa.department.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sancai.oa.company.entity.Company;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.department.entity.DepartmentDTO;
import com.sancai.oa.department.entity.TDepartment;
import com.sancai.oa.department.mapper.TDepartmentMapper;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.department.service.ITDepartmentService;
import com.sancai.oa.quartz.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author wangyl
 * @create 2019/7/25 09:14
 */
@Slf4j
@Service
public class DepartmentServiceImpl implements IDepartmentService {


    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private TDepartmentMapper tDepartmentMapper;

    @Override
    public DepartmentDTO getDepartmentByCompayid(String company_id) {
        DepartmentDTO departmentDto = new DepartmentDTO();
        // redis 获取
        List<Department> departmentList= listDepartment(company_id);
        //获取根节点
        for (Department res : departmentList) {
            if ("1".equals(res.getId())) {
                departmentDto.setId(res.getId());
                departmentDto.setLevel(res.getLevel());
                departmentDto.setName(res.getName());
                departmentDto.setParentid("-1");
                departmentDto.setChildren(new ArrayList<>());
            }
        }
        //获取根节点下的子部门信息
        departmentDto = findChildrenTreebyList(departmentDto, departmentList);
        return departmentDto;
    }

    @Override
    public DepartmentDTO findChildrenTreebyList(DepartmentDTO root, List<Department> departmentList) {
        for (Department d : departmentList) {
            if (root.getId().equals(d.getParentid())) {
                if (null == root.getChildren()) {
                    root.setChildren(new ArrayList<DepartmentDTO>());
                }
                DepartmentDTO tmp = new DepartmentDTO();
                tmp.setId(d.getId() + "");
                tmp.setParentid(d.getParentid() + "");
                tmp.setName(d.getName());
                tmp.setLevel(d.getLevel());

                DepartmentDTO dtree = findChildrenTreebyList(tmp, departmentList);

                root.getChildren().add(dtree);
            }
        }
        return root;
    }

    @Override
    public Department getDepartment(String dept_id) {
        Department department = new Department();
        Map m = redisUtil.hmget(dept_id);
        if(m.isEmpty()){
            QueryWrapper<TDepartment> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TDepartment::getDeleted, 0);
            queryWrapper.lambda().eq(TDepartment::getDeptId, dept_id);
            TDepartment tDepartment = tDepartmentMapper.selectOne(queryWrapper);
            if (tDepartment != null) {
                department.setParentid(tDepartment.getParentId());
                department.setId(tDepartment.getDeptId());
                department.setName(tDepartment.getDeptName());
                if(null!=tDepartment.getLevel()){
                    department.setLevel(tDepartment.getLevel().longValue());
                }

                Map hm = new HashMap<>();
                hm.put("id", tDepartment.getDeptId());
                hm.put("parentid", tDepartment.getParentId());
                hm.put("name", tDepartment.getDeptName());
                if(null!=tDepartment.getLevel()){
                    hm.put("level", tDepartment.getLevel().longValue());
                }


                //存储部门信息
                redisUtil.hmset(hm.get("id") + "", hm, 24 * 60 * 60);

                return department;
            }else {
                return null;
            }
        }
        department.setParentid(m.get("parentid") + "");
        department.setId(m.get("id") + "");
        department.setName(m.get("name") + "");
        department.setLevel(Long.parseLong(m.get("level") + ""));
        return department;
    }

    @Override
    public List<Department> listDepartment(String company_id) {
        List<Department> result = new ArrayList<Department>();
        QueryWrapper<TDepartment> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TDepartment::getDeleted, 0);
        queryWrapper.lambda().eq(TDepartment::getCompanyId, company_id);
        List<TDepartment> tDepartmentList = tDepartmentMapper.selectList(queryWrapper);

        for (Object ob : tDepartmentList) {
            TDepartment tDepartment = (TDepartment) ob;
            Department department = new Department();
            department.setId(tDepartment.getDeptId());
            department.setName(tDepartment.getDeptName());
            department.setParentid(tDepartment.getParentId());
            if(null!=tDepartment.getLevel()){
                department.setLevel(tDepartment.getLevel().longValue());
            }

            result.add(department);
        }
        return result;
    }


    @Override
    public List<Long> listSubDepartment(String companyId, String deptId) {
        DepartmentDTO departmentDto = new DepartmentDTO();
        departmentDto.setId(deptId);
        // redis 获取
        List<Department> result = listDepartment(companyId);

        //获取部门下的子部门信息
        List<DepartmentDTO> departmentList=  findChildrenDepartmentbyList(departmentDto, result);
        List<Long> longList = new ArrayList<Long>();
        for (DepartmentDTO departmentDTO:departmentList){
            longList.add(Long.parseLong(departmentDTO.getId()));
        }
        return longList;
    }

    private List<DepartmentDTO> findChildrenDepartmentbyList(DepartmentDTO subDepartment, List<Department> departmentList) {
        List<DepartmentDTO> resList = new ArrayList<DepartmentDTO>();
        resList.add(subDepartment);
        for (Department d : departmentList) {
            if (subDepartment.getId().equals(d.getParentid())) {
                DepartmentDTO tmp = new DepartmentDTO();
                tmp.setId(d.getId() + "");
                tmp.setParentid(d.getParentid() + "");
                tmp.setName(d.getName());
                tmp.setLevel(d.getLevel());
                resList.add(tmp);
                resList.addAll(findChildrenDepartmentbyList(tmp,departmentList));

            }
        }
        return resList;
    }

    @Override
    public String getSuperiorsDepartmentName(String companyId,String deptId) {
        String res = "";
        List<Department> result = this.listDepartment(companyId);
        List<String> deptlist = new ArrayList<String>();
        Map deptmap = new HashMap();
        for (Department d : result) {
            deptmap.put(d.getId(), d);
        }
        Department department =(Department) deptmap.get(deptId);

        if(null!=department){
            while(!department.getId().equals("1")){
                if(StringUtils.isEmpty(res)){
                    res = department.getName();
                }else{
                    res = department.getName()+"-"+res;
                }

                department =(Department) deptmap.get(department.getParentid());
                if(null==department){
                    break;
                }

            }
        }


        return res;
    }


}