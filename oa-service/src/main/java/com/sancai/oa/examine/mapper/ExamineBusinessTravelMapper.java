package com.sancai.oa.examine.mapper;

import com.sancai.oa.examine.entity.ExamineBusinessTravel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.examine.entity.ExamineBusinessTravelDTO;
import com.sancai.oa.examine.entity.ExamineBusinessTravelDetailDTO;
import com.sancai.oa.examine.entity.RequestEntity;
import com.sancai.oa.report.entity.modify.DataMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 出差 Mapper 接口
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-24
 */
@Repository
public interface ExamineBusinessTravelMapper extends BaseMapper<ExamineBusinessTravel> {

    /**
     *
     * @param requestEntity 查询出差记录列表
     * @return 返回数据对象的集合
     */
    List<ExamineBusinessTravelDTO> getBusinessTravelListByPage(RequestEntity requestEntity);


    /**
     *
     * @param  id 出差记录
     * @return 返回列表，因为一人可能属多个部门
     */

    ExamineBusinessTravelDetailDTO getBusinessTravelDetails(String id);

    /**
     * 记录部门关系数据
     * @param
     * @return
     */
    List<DataMap> businessRecordDeptIdList(@Param(value = "companyId") String companyId , @Param(value = "taskInstanceId") String taskInstanceId);

}
