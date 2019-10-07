package com.sancai.oa.examine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.examine.entity.ActionPositionDataDTO;
import com.sancai.oa.examine.entity.ExamineAction;
import com.sancai.oa.examine.entity.RequestEntity;
import com.sancai.oa.report.entity.modify.DataMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 行为奖罚 Mapper 接口
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Repository
public interface ExamineActionMapper extends BaseMapper<ExamineAction> {
    /**
     * @param requestEntity 行为考核实体类
     * @return 获取行为考核记录分页列表
     */
    List<ActionPositionDataDTO> getExamineListByPage(RequestEntity requestEntity);

    /**
     * 记录部门关系数据
     * @param
     * @return
     */
    List<DataMap> actionRecordDeptIdList(@Param(value = "companyId") String companyId , @Param(value = "taskInstanceId") String taskInstanceId);

}
