package com.sancai.oa.score.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dingtalk.api.response.OapiUserGetResponse;
import com.github.pagehelper.PageHelper;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.department.service.IDepartmentService;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.examine.utils.QueryCommonUtils;
import com.sancai.oa.examine.utils.UUIDS;
import com.sancai.oa.score.entity.*;
import com.sancai.oa.score.exception.EnumScoreError;
import com.sancai.oa.score.exception.OaScoreException;
import com.sancai.oa.score.mapper.ActionScoreRecordMapper;
import com.sancai.oa.score.service.IActionScoreDepartmentService;
import com.sancai.oa.score.service.IActionScoreRecordService;
import com.sancai.oa.typestatus.enums.ScoreRecordSourceEnum;
import com.sancai.oa.typestatus.enums.ScoreRecordTypeEnum;
import com.sancai.oa.utils.LocalDateTimeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.omg.CORBA.Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 行为积分变动记录 服务实现类
 * </p>
 *
 * @author quanleilei
 * @since 2019-07-31
 */
@Service
public class ActionScoreRecordServiceImpl extends ServiceImpl<ActionScoreRecordMapper, ActionScoreRecord> implements IActionScoreRecordService {

    @Autowired
    ActionScoreRecordMapper actionScoreRecordMapper;

    @Autowired
    DingDingUserService dingDingUserService;

    @Autowired
    RedisUtil redisUtil;

    @Autowired
    private DingDingReportService dingDingReportService;

    @Autowired
    private IActionScoreDepartmentService actionScoreDepartmentService;

    @Autowired
    private IDepartmentService departmentService;

    /**
     * 对一名员工进行积分的变动（增加/删除）
     *
     * @param actionScoreRecord 积分记录表的实体类
     * @return 返回受影响的行数
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Integer modifyScoreRecord(ActionScoreRecord actionScoreRecord) {
        String companyId = actionScoreRecord.getCompanyId();
        String userId = actionScoreRecord.getUserId();
        //设置id
        actionScoreRecord.setId(UUIDS.getID());
        long currentTimeMillis = System.currentTimeMillis();
        //设置创建时间
        actionScoreRecord.setCreateTime(currentTimeMillis);
        //设置记录生成时间
        actionScoreRecord.setScoreRecordTime(currentTimeMillis);
        //设置是否删除（默认未删 0）
        actionScoreRecord.setDeleted(0);
        int result = actionScoreRecordMapper.insert(actionScoreRecord);
        if (result != 1) {
            throw new OaScoreException(EnumScoreError.INSERT_USERSCORE_FAILURE);
        }
        //积分变动列表插入记录的同时往部门关系表插入记录（多个部门则为多条记录）
        OapiUserGetResponse oapiUserGetResponse = dingDingReportService.userinfoByUserId(userId, companyId);
        List<Long> department = oapiUserGetResponse.getDepartment();
        //该员工的部门id列表（一个或多个）
        List<ActionScoreDepartment> actionScoreDepartments = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(department)) {
            department.stream().forEach(departId -> {
                ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
                actionScoreDepartment.setId(UUIDS.getID());
                actionScoreDepartment.setScoreRecordId(actionScoreRecord.getId());
                actionScoreDepartment.setDeptId(new Long(departId).intValue());
                actionScoreDepartment.setCreateTime(currentTimeMillis);
                actionScoreDepartment.setDeleted(0);
                actionScoreDepartments.add(actionScoreDepartment);
            });
        }
        if (CollectionUtils.isNotEmpty(actionScoreDepartments)) {
            for (ActionScoreDepartment actionScoreDepartment : actionScoreDepartments) {
                actionScoreDepartmentService.save(actionScoreDepartment);
            }
        }
        return result;
    }

    /**
     * 积分变动列表
     *
     * @param scoreRecordRequestDTO 请求体的封装类
     * @return 返回Map的集合
     */
    @Override
    public List<ScoreRecordListDTO> getScoreRecordList(ScoreRecordRequestDTO scoreRecordRequestDTO) {
        //从redis中取出companyId对应的部门列表
        Set deptSet = redisUtil.sGet(scoreRecordRequestDTO.getCompanyId());
        //部门查询条件存在时，查询部门及其子部门的所有信息
        String deptId = scoreRecordRequestDTO.getDeptId();
        if(StringUtils.isNotBlank(deptId)) {
            List<Long> deptList = departmentService.listSubDepartment(scoreRecordRequestDTO.getCompanyId(), scoreRecordRequestDTO.getDeptId());
            scoreRecordRequestDTO.setDeptList(deptList);
        }
        //分页
        PageHelper.startPage(scoreRecordRequestDTO.getPage(), scoreRecordRequestDTO.getCapacity());
        List<ScoreRecordListDTO> scoreRecordList = actionScoreRecordMapper.getScoreRecordList(scoreRecordRequestDTO);
        //调用工具类
        if (CollectionUtils.isNotEmpty(scoreRecordList)) {
            QueryCommonUtils.setDeptNameAll(scoreRecordList, deptSet);
            for (ScoreRecordListDTO scoreRecordListDTO : scoreRecordList) {
                //将类型和状态转换为中文
                String type = ScoreRecordTypeEnum.getvalueBykey(scoreRecordListDTO.getType());
                String source = ScoreRecordSourceEnum.getvalueBykey(scoreRecordListDTO.getSource());
                scoreRecordListDTO.setType(type);
                scoreRecordListDTO.setSource(source);
            }
        }
        return scoreRecordList;
    }

    /**
     * 根据userId查询一名员工的目前累计的或者指定一个月的积分总和
     *
     * @param user_id   用户id
     * @param startTime 一个月的开始时间
     * @param endTime   一个月的结束时间
     * @return 返回该员工对应的userId, userName, score
     */
    @Override
    public ActionUserScoreDTO queryUserScore(String user_id, String companyId,Long startTime, Long endTime) {
        ActionScoreRecordDTO scoreRecordDTO = new ActionScoreRecordDTO();
        scoreRecordDTO.setUserId(user_id);
        scoreRecordDTO.setStartTime(startTime);
        scoreRecordDTO.setEndTime(endTime);
        scoreRecordDTO.setCompanyId(companyId);
        return actionScoreRecordMapper.queryUserScore(scoreRecordDTO);

    }

    /**
     * 部门下的员工积分列表
     *
     * @param dept_id 部门id
     * @param page 页数
     * @param capacity 容量
     * @return 返回该部门下的所有用户信息（user_id,name,total_score）
     */
    @Override
    public List getDeptUserList(String dept_id,String company_id,Integer page,Integer capacity) {
        //查询部门及可能存在的子部门的所有信息
            List<Long> deptList = departmentService.listSubDepartment(company_id, dept_id);
        //分页
        PageHelper.startPage(page, capacity);
        List<Map> list = actionScoreRecordMapper.getDeptScoreList(deptList);
        return list;
    }

    @Override
    public Boolean userIsWarned(String userId,String companyId,Long reportTime ,String enumScoreRule) {
        LocalDateTime localDateTime = LocalDateTimeUtils.getDateTimeOfTimestamp(reportTime);
        LocalDateTime firstDayOfMonth = LocalDateTimeUtils.getDayStart(
                localDateTime.with(TemporalAdjusters.firstDayOfMonth()));
        LocalDateTime lastDayOfMonth = LocalDateTimeUtils.getDayEnd(
                localDateTime.with(TemporalAdjusters.lastDayOfMonth()));
        Long startTime = LocalDateTimeUtils.getMilliByTime(firstDayOfMonth);
        Long endTime = LocalDateTimeUtils.getMilliByTime(lastDayOfMonth);
        Integer count = actionScoreRecordMapper.reportisWarnedCount(userId,companyId,enumScoreRule,startTime,endTime);
        if(count != null && count > 0){
            return true;
        }
        return false;
    }
    @Override
    public Boolean userIsWarned(String userId,String companyId,Long reportTime ,String enumScoreRule,List<ActionScoreRecord> actionScoreRecordList ,String taskInstanceId) {
        LocalDateTime localDateTime = LocalDateTimeUtils.getDateTimeOfTimestamp(reportTime);
        LocalDateTime firstDayOfMonth = LocalDateTimeUtils.getDayStart(
                localDateTime.with(TemporalAdjusters.firstDayOfMonth()));
        LocalDateTime lastDayOfMonth = LocalDateTimeUtils.getDayEnd(
                localDateTime.with(TemporalAdjusters.lastDayOfMonth()));
        Long startTime = LocalDateTimeUtils.getMilliByTime(firstDayOfMonth);
        Long endTime = LocalDateTimeUtils.getMilliByTime(lastDayOfMonth);
            //查询本次任务执行前，日报提交时间所在的月份中是否已经生成警告数据
        List<ActionScoreRecord> scoreRecord = actionScoreRecordList.stream().filter(ActionScoreRecord ->
                ActionScoreRecord.getUserId().equals(userId)
                        && ActionScoreRecord.getCreateTime() >=  startTime
                        && ActionScoreRecord.getCreateTime() <=  endTime
                        && ActionScoreRecord.getSource().equals(enumScoreRule)).collect(Collectors.toList());
        if(scoreRecord != null && scoreRecord.size()>0){
            return true;
        }
        //查询本次执行任务中，日报提交时间所在的月份中是否已经生成警告数据
        List<ActionScoreRecord> actionScoreRecordListRedis  = JSON.parseArray((String) redisUtil.get(taskInstanceId + "REPORT_WARNING"),ActionScoreRecord.class);
        if(actionScoreRecordListRedis == null || actionScoreRecordListRedis.size() == 0){
            return false;
        }
        List<ActionScoreRecord> signinRecords = null;
            signinRecords = actionScoreRecordListRedis.stream().filter(ActionScoreRecord ->
                    ActionScoreRecord.getUserId().equals(userId)
                            && ActionScoreRecord.getCreateTime() >=  startTime
                            && ActionScoreRecord.getCreateTime() <=  endTime
                            && ActionScoreRecord.getSource().equals(enumScoreRule)).collect(Collectors.toList());
        if(signinRecords != null && signinRecords.size()>0){
            return true;
        }
        return false;
    }
}

