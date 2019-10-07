package com.sancai.oa.report.thread;

import com.sancai.oa.clockin.enums.EnumScoreRule;
import com.sancai.oa.examine.entity.ExamineHoliday;
import com.sancai.oa.examine.entity.ExamineLeave;
import com.sancai.oa.score.service.IActionScoreDeductService;
import com.sancai.oa.score.service.IActionScoreRecordService;
import com.sancai.oa.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 未提交日报的用户检查
 * @Author chenm
 * @create 2019/8/1 15:33
 */

@Slf4j
public class NoReportUserThreadTask implements Runnable{

    private String companyId;

    private List<User> userList;

    private long intervalTimeEnd;


    //请假集合
    private List<ExamineLeave> examineLeaves;
    //公休集合
    private List<ExamineHoliday> examineHolidays;
    @Autowired
    private IActionScoreDeductService actionScoreDeductService;

    @Autowired
    private IActionScoreRecordService actionScoreRecordService;



    public NoReportUserThreadTask(String companyId,List<User> userList, List<ExamineLeave> examineLeaves,List<ExamineHoliday> examineHolidays, IActionScoreDeductService actionScoreDeductService,long intervalTimeEnd,IActionScoreRecordService actionScoreRecordService) {
        this.userList = userList;
        this.companyId = companyId;
        this.examineLeaves = examineLeaves;
        this.examineHolidays = examineHolidays;
        this.actionScoreDeductService = actionScoreDeductService;
        this.intervalTimeEnd = intervalTimeEnd;
        this.actionScoreRecordService = actionScoreRecordService;
    }

    private void checkUserReport(){

        for (User user : userList){

            // 3.1 调用判断审批是否有效方法 -- 开始
            boolean isNotValid = examineValid(user,examineLeaves,examineHolidays);
            // 3.1 调用判断审批是否有效方法 -- 结束
            if (!isNotValid) {
                //3.2 审批无效，调用扣积分方法
                //调用扣积分方法--开始
                Boolean isWarned = actionScoreRecordService.userIsWarned(user.getUserId(),companyId,intervalTimeEnd,EnumScoreRule.REPORLATEDELIVERYESIXTYMINUTESMORE.getKey());
                if(!isWarned){
                    //actionScoreDeductService.reportDeductScore(companyId,user,EnumScoreRule.REPORLATEDELIVERYESIXTYMINUTESMORE.getKey());
                }else{
                    //调用接口，对于日报未提交进行扣分
                   // actionScoreDeductService.reportDeductScore(companyId,user,EnumScoreRule.REPORLATEDELIVERYESIXTYMINUTESMORE.getKey());
                }
                //调用扣积分方法--结束
            }
        }
    }

    /**
     * 调用判断审批是否有效方法
     * @param user
     * @param examineLeaves
     * @param examineHolidays
     * @return
     */
    private  boolean examineValid (User user , List<ExamineLeave> examineLeaves , List<ExamineHoliday> examineHolidays){
        List<ExamineLeave>  examineLeaveList = examineLeaves.stream().filter(ExamineLeave -> ExamineLeave.getUserId().equals(user.getUserId())).collect(Collectors.toList());
        List<ExamineHoliday>  examineHolidayList = examineHolidays.stream().filter(ExamineHoliday -> ExamineHoliday.getUserId().equals(user.getUserId())).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(examineLeaveList) && CollectionUtils.isEmpty(examineHolidayList)){
            return false;
        }
        return true;
    }


    @Override
    public void run() {
        try {
            checkUserReport();
        } catch (Exception e) {
            log.error("ClockinThreadTask error:"+e.getMessage());
        }
    }
}
