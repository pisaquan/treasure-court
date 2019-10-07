package com.sancai.oa.clockin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.clockin.entity.ClockinRecord;
import com.sancai.oa.clockin.entity.ClockinRecordDTO;
import com.sancai.oa.report.entity.modify.DataMap;

import java.util.List;

/**
 * <p>
 * 考勤打卡记录 服务类
 * </p>
 *
 * @author fans
 * @since 2019-07-26
 */
public interface IClockinRecordService extends IService<ClockinRecord> {
    /**
     * 公司打卡记录列表
     *
     * @param clockinRecordDTO
     * @return
     */

    List<DataMap> clockinRecordList(ClockinRecordDTO clockinRecordDTO);

    /**
     * 公司打卡记录详情
     *
     * @param id
     * @return
     */

    ClockinRecordDTO clockinRecordDetail(String id);

    int deleteByTaskInstanceId(String taskIntanceId);


}
