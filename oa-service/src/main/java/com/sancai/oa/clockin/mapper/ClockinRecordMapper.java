package com.sancai.oa.clockin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.clockin.entity.ClockinRecordDTO;
import com.sancai.oa.report.entity.modify.DataMap;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 考勤打卡记录 Mapper 接口
 * </p>
 *
 * @author fans
 * @since 2019-07-26
 */
@Repository
public interface ClockinRecordMapper extends BaseMapper<ClockinRecord> {

    /**
     * 条件筛选考勤打卡记录列表
     * @param clockinRecordDTO
     * @return  List
     */
    public List<DataMap> clockinRecordList(ClockinRecordDTO clockinRecordDTO);


    public ClockinRecordDTO  clockinRecordDetailById(String id);

    /**
     * 按任务实例id删除
     * @param taskIntanceId
     */
    public int deleteByTaskInstanceId(String taskIntanceId);
}
