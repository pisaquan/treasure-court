package com.sancai.oa.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.signin.entity.SigninRecord;
import com.sancai.oa.user.entity.UserDepartment;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDepartmentMapper extends BaseMapper<UserDepartment> {
    /**
     * 批量保存数据
     *
     * @param userDepartmentList 数据集合
     * @return
     */
    void batchSave(List<UserDepartment> userDepartmentList);
}
