package com.sancai.oa.signin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.report.entity.modify.DataMap;
import com.sancai.oa.signin.entity.SigninRecord;
import com.sancai.oa.signin.entity.SigninRecordDTO;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * <p>
 * 签到记录 Mapper 接口
 * </p>
 *
 * @author fans
 * @since 2019-07-19
 */
@Repository
public interface SigninRecordMapper extends BaseMapper<SigninRecord> {



    /**
     * 签到记录列表
     * @param signinRecordDTO
     * @return
     */
    List<DataMap> signinRecordList(SigninRecordDTO signinRecordDTO);


    /**
     * 根据id修改签到记录状态
     * @param tSigninRecord
     * @return
     */
   int  updateTsigninRecordStateById(SigninRecord tSigninRecord);
    /**
     * 批量保存数据
     *
     * @param signinRecordList 数据集合
     * @return
     */
    void batchSave(List<SigninRecord> signinRecordList);

    /**
     * 记录部门关系数据
     * @param
     * @return
     */
    List<DataMap> signinRecordDeptIdList(@Param(value = "companyId") String companyId ,@Param(value = "taskInstanceId") String taskInstanceId);

}
