package com.sancai.oa.score.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.entity.ActionUserScoreDTO;
import com.sancai.oa.score.entity.ScoreRecordListDTO;
import com.sancai.oa.score.entity.ScoreRecordRequestDTO;

import java.util.List;


/**
 * <p>
 * 行为积分变动记录 服务类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
public interface IActionScoreRecordService extends IService<ActionScoreRecord> {

    /**
     * 对一名员工进行积分的变动
     *
     * @param actionScoreRecord 请求体的封装类
     * @return 返回受影响的行数
     */
    Integer modifyScoreRecord(ActionScoreRecord actionScoreRecord);

    /**
     * 积分变动列表
     *
     * @param scoreRecordRequestDTO 请求体的封装类
     * @return 返回Map的集合
     */
    List<ScoreRecordListDTO> getScoreRecordList(ScoreRecordRequestDTO scoreRecordRequestDTO);

    /**
     * 查询一名员工的目前累计的积分总和
     *
     * @param user_id 员工id
     * @return 返回 user_id,user_name,score（总积分）
     */
    ActionUserScoreDTO queryUserScore(String user_id, String companyId, Long startTime, Long endTime);

    /**
     * 部门下的员工积分列表
     *
     * @param dept_id 部门id
     * @param page 页数
     * @param capacity 容量
     * @return 返回该部门下的所有用户信息（user_id,name,total_score）
     */
    List getDeptUserList(String dept_id,String company_id,Integer page,Integer capacity);
    /**
     * 判断员工这个月是否被警告过（不扣积分）
     * @param userId
     * @param companyId
     * @return
     */
    Boolean userIsWarned(String userId,String companyId,Long reportTime ,String enumScoreRule);
    /**
     * 判断员工这个月是否被警告过（不扣积分）
     * @param userId
     * @param companyId
     * @return
     */
    Boolean userIsWarned(String userId,String companyId,Long reportTime ,String enumScoreRule,List<ActionScoreRecord> actionScoreRecordList,String taskInstanceId);
}
