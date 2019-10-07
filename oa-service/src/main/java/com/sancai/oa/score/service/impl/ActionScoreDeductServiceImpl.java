package com.sancai.oa.score.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sancai.oa.clockin.enums.EnumScoreRule;
import com.sancai.oa.core.redis.RedisUtil;
import com.sancai.oa.dingding.report.DingDingReportService;
import com.sancai.oa.dingding.user.DingDingUserService;
import com.sancai.oa.report.entity.ReportRecord;
import com.sancai.oa.score.entity.ActionScoreDepartment;
import com.sancai.oa.score.entity.ActionScoreRecord;
import com.sancai.oa.score.entity.ActionScoreRule;
import com.sancai.oa.score.entity.ActionScoreRuleDTO;
import com.sancai.oa.score.exception.EnumScoreError;
import com.sancai.oa.score.exception.OaScoreException;
import com.sancai.oa.score.mapper.ActionScoreRecordMapper;
import com.sancai.oa.score.mapper.ActionScoreRuleMapper;
import com.sancai.oa.score.service.IActionScoreDeductService;
import com.sancai.oa.score.service.IActionScoreDepartmentService;
import com.sancai.oa.user.entity.User;
import com.sancai.oa.user.entity.UserDTO;
import com.sancai.oa.user.entity.UserDepartment;
import com.sancai.oa.utils.UUIDS;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * 判断日报是否符合规则积分扣除接口
 *
 * @author fanjing
 * @date 2019/8/10
 */
@Service
public class ActionScoreDeductServiceImpl implements IActionScoreDeductService {


    @Autowired
    ActionScoreRecordMapper actionScoreRecordMapper;

    @Autowired
    DingDingUserService dingDingUserService;

    @Autowired
    private DingDingReportService dingDingReportService;

    @Autowired
    private IActionScoreDepartmentService actionScoreDepartmentService;

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    ActionScoreRuleMapper actionScoreRuleMapper;
    /**
     * 根据传来的ruleKey在积分规则中找出应扣分数后再积分记录表中添加记录
     *
     * @param companyId 公司Id
     * @param user   用户
     * @param ruleKey   ruleKey为积分规则中的规则Key字段
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void reportDeductScore(String companyId, UserDTO user, String ruleKey , ReportRecord reportRecord  , String remark , String taskInstanceId) {
        ActionScoreRuleDTO actionScoreRuleDTO = actionScoreRuleMapper.getScoreRuleByKey(ruleKey);
        if (actionScoreRuleDTO != null ) {
            ActionScoreRecord record = new ActionScoreRecord();
            //设置积分记录的各字段属性值
            record.setScore(Float.valueOf(actionScoreRuleDTO.getScore()));
            record.setType(actionScoreRuleDTO.getType());
            record.setCompanyId(companyId);
            record.setUserId(user.getUserId());
            record.setUserName(user.getName());
            record.setCreateTime(System.currentTimeMillis());
            record.setSource(ruleKey);
            record.setId(UUIDS.getID());
            record.setTargetId(taskInstanceId+"-"+reportRecord.getId());
            record.setRemark(remark);
            record.setScoreRecordTime(reportRecord.getReportTime());
            int i = actionScoreRecordMapper.insert(record);
            if (i == 0) {
                throw new OaScoreException(EnumScoreError.INSERT_SCORERECORD_FAILURE);
            }

            if (user.getUserDepartments() == null || user.getUserDepartments().size() == 0) {
                return;
            }
            //积分变动列表插入记录的同时往部门关系表插入记录（多个部门则为多条记录）
            List<ActionScoreDepartment> actionScoreDepartments = new ArrayList<>();

            for(UserDepartment userDepartment : user.getUserDepartments()){
                String deptId = userDepartment.getDeptId();
                ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
                actionScoreDepartment.setId(com.sancai.oa.examine.utils.UUIDS.getID());
                actionScoreDepartment.setScoreRecordId(record.getId());
                actionScoreDepartment.setDeptId(new Long(deptId).intValue());
                actionScoreDepartment.setCreateTime(System.currentTimeMillis());
                actionScoreDepartment.setDeleted(0);
                actionScoreDepartments.add(actionScoreDepartment);
			}
            
            if (CollectionUtils.isNotEmpty(actionScoreDepartments)) {
                for (ActionScoreDepartment actionScoreDepartment : actionScoreDepartments) {
                    actionScoreDepartmentService.save(actionScoreDepartment);
                }
            }
            

        }
    }
    /**
     * 日报积分订正
     *
     * @param companyId 公司Id
     * @param user   用户
     * @param ruleKey   ruleKey为积分规则中的规则Key字段
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public void reportDeductScore(String companyId, UserDTO user, String ruleKey , ReportRecord reportRecord  , String remark , ActionScoreRecord actionScoreRecord) {
        ActionScoreRuleDTO actionScoreRuleDTO = actionScoreRuleMapper.getScoreRuleByKey(ruleKey);
        if (actionScoreRuleDTO != null && actionScoreRecord != null) {
            ActionScoreRecord record = new ActionScoreRecord();
            //设置积分记录的各字段属性值
            record.setScore(Float.valueOf(actionScoreRecord.getScore()));
            record.setType(actionScoreRuleDTO.getType());
            record.setCompanyId(companyId);
            record.setUserId(user.getUserId());
            record.setUserName(user.getName());
            record.setCreateTime(System.currentTimeMillis());
            record.setSource(ruleKey);
            record.setId(UUIDS.getID());
            record.setTargetId(actionScoreRecord.getTargetId());
            record.setRemark(remark+"(日报积分订正)");
            record.setScoreRecordTime(reportRecord.getReportTime());
            int i = actionScoreRecordMapper.insert(record);
            if (i == 0) {
                throw new OaScoreException(EnumScoreError.INSERT_SCORERECORD_FAILURE);
            }
            if (user.getUserDepartments() == null || user.getUserDepartments().size() == 0) {
                return;
            }
            //积分变动列表插入记录的同时往部门关系表插入记录（多个部门则为多条记录）
            List<ActionScoreDepartment> actionScoreDepartments = new ArrayList<>();

            for(UserDepartment userDepartment : user.getUserDepartments()){
                String deptId = userDepartment.getDeptId();
                ActionScoreDepartment actionScoreDepartment = new ActionScoreDepartment();
                actionScoreDepartment.setId(com.sancai.oa.examine.utils.UUIDS.getID());
                actionScoreDepartment.setScoreRecordId(record.getId());
                actionScoreDepartment.setDeptId(new Long(deptId).intValue());
                actionScoreDepartment.setCreateTime(System.currentTimeMillis());
                actionScoreDepartment.setDeleted(0);
                actionScoreDepartments.add(actionScoreDepartment);
            }

            if (CollectionUtils.isNotEmpty(actionScoreDepartments)) {
                for (ActionScoreDepartment actionScoreDepartment : actionScoreDepartments) {
                    actionScoreDepartmentService.save(actionScoreDepartment);
                }
            }


        }
    }
}
