package com.sancai.oa.score.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sancai.oa.score.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 行为积分变动记录 Mapper 接口
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@Repository
public interface ActionScoreRecordMapper extends BaseMapper<ActionScoreRecord> {

    /**
     * 积分变动列表
     * @param scoreRecordRequestDTO 请求体的封装类
     * @return 返回Map的集合
     */
    List<ScoreRecordListDTO> getScoreRecordList(ScoreRecordRequestDTO scoreRecordRequestDTO);

    /**
     * 查询一名员工的目前累计的或者指定一个月的积分总和
     * @param actionScoreRecordDTO 一个月的结束时间
     * @return 返回该用户id对应的所有记录
     */
    ActionUserScoreDTO queryUserScore(ActionScoreRecordDTO actionScoreRecordDTO);


    /**
     * 部门下的员工积分列表
     * @param deptList 部门集合
     * @return 返回该部门下的所有用户信息（user_id,name,total_score）
     */
    List<Map> getDeptScoreList(@Param("deptList") List<Long> deptList);


    /**
     * 根据规则标识key查询对应的score值
     * @param ruleKey 对应规则标识key
     * @return 返回score的值
     */
    ActionScoreRecord getScoreByRuleKey(String ruleKey);

    /**
     * 查询日报晚交第一次警告
     * @param userId
     * @param companyId
     * @param enumScoreRule
     * @return
     */
    Integer reportisWarnedCount(@Param("userId") String userId,@Param("companyId") String companyId,@Param("enumScoreRule") String enumScoreRule,@Param("startTime") Long startTime,@Param("endTime") Long endTime);
	
    /**
     * 根据公司id查询有效的日报积分记录数据和对应日报提交时间
     * @param companyId
     * @return
     */
    List<ActionScoreRecord> getScoreRecordListByCompanyId(String companyId);


    List<ActionScoreRecord> getScoreRecordListByUserId(ActionScoreRecord actionScoreRecord);

}
