package com.sancai.oa.examine.mapper;

import com.sancai.oa.examine.entity.ExamineHoliday;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.examine.entity.ExamineHolidayDTO;
import com.sancai.oa.examine.entity.ExamineHolidayDetailDTO;
import com.sancai.oa.examine.entity.RequestEntity;
import com.sancai.oa.report.entity.modify.DataMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 公休假 Mapper 接口
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Repository
public interface ExamineHolidayMapper extends BaseMapper<ExamineHoliday> {

    /**
     * 获取条件查询公休记录列表（条件查询数据可能为空）
     * @param requestEntity 将请求体中数据封装到ExamineHolidy中
     * @return 返回公休列表记录
     */
    List<ExamineHolidayDTO> getExamineHolidayList(RequestEntity requestEntity);

    /**
     * 根据公休记录id获取详情记录
     * @param id 公休记录id
     * @return 返回一条公休记录详情
     */
    ExamineHolidayDetailDTO getHolidayDetail(String id);
    /**
     * 记录部门关系数据
     * @param
     * @return
     */
    List<DataMap> holidayRecordDeptIdList(@Param(value = "companyId") String companyId , @Param(value = "taskInstanceId") String taskInstanceId);
}
