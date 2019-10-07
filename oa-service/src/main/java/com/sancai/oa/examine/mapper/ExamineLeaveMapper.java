package com.sancai.oa.examine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.examine.entity.ExamineLeave;
import com.sancai.oa.examine.entity.ExamineLeaveDTO;
import com.sancai.oa.examine.entity.ExamineLeaveDetailDTO;
import com.sancai.oa.examine.entity.RequestEntity;
import com.sancai.oa.report.entity.modify.DataMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 请假 Mapper 接口
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Repository
public interface ExamineLeaveMapper extends BaseMapper<ExamineLeave> {

    /**
     * 请假列表查询
     *
     * @param requestEntity 请求体封装在ExamineLeave实体类中
     * @return 返回ExmineLeaveDTO集合
     */
    List<ExamineLeaveDTO> getExamineLeaveList(RequestEntity requestEntity);

    /**
     * 请假详情查询
     *
     * @param id 请假记录id
     * @return 返回请假记录详情
     */
    ExamineLeaveDetailDTO getExamineLeaveDetail(String id);
    /**
     * 记录部门关系数据
     * @param
     * @return
     */
    List<DataMap> leaveRecordDeptIdList(@Param(value = "companyId") String companyId , @Param(value = "taskInstanceId") String taskInstanceId);

    /**
     * 请假详情查询病假图片
     *
     * @param id 请假记录id
     * @return 返回请假记录详情
     */
    ExamineLeaveDetailDTO getExamineLeaveDetailImg(String id);

}
