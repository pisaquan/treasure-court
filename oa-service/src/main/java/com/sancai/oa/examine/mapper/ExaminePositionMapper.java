package com.sancai.oa.examine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.examine.entity.ActionPositionDataDTO;
import com.sancai.oa.examine.entity.ExaminePosition;
import com.sancai.oa.examine.entity.RequestEntity;
import com.sancai.oa.report.entity.modify.DataMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 岗位奖罚 Mapper 接口
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Repository
public interface ExaminePositionMapper extends BaseMapper<ExaminePosition> {
    /**
     * @param requestEntity 封装查询条件的对象
     * @return 返回DTO对象集合
     */
    List<ActionPositionDataDTO> getExamineListByPage(RequestEntity requestEntity);

    /**
     * 记录部门关系数据
     * @param
     * @return
     */
    List<DataMap> positionRecordDeptIdList(@Param(value = "companyId") String companyId , @Param(value = "taskInstanceId") String taskInstanceId);
}
