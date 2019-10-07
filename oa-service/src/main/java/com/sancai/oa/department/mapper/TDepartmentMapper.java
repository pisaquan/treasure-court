package com.sancai.oa.department.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.department.entity.TDepartment;
import com.sancai.oa.user.entity.UserDepartment;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 部门表 Mapper 接口
 * </p>
 *
 * @author wangyl
 * @since 2019-09-25
 */
@Repository
public interface TDepartmentMapper extends BaseMapper<TDepartment> {
    /**
     * 批量保存数据
     *
     * @param tDepartmentList 数据集合
     * @return
     */
    void batchSave(List<TDepartment> tDepartmentList);
}
