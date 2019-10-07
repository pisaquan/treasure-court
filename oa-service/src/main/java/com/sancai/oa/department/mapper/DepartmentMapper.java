package com.sancai.oa.department.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.department.entity.Department;
import com.sancai.oa.examine.entity.ExamineBusinessTravel;
import com.sancai.oa.user.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 部门接口类
 * @Author wangyl
 * @create 2019/7/25 09:14
 */
@Repository
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 部门详情
     * @param id
     * @return
     */
    User getDepartment(String id);

    /**
     * 部门列表
     * @return
     */
    List<Department> listDepartment();

    /**
     * 新增部门
     * @param department
     */
    void saveDepartment(Department department);

}
