package com.sancai.oa.report.entity;

import com.sancai.oa.examine.entity.ExamineHoliday;
import com.sancai.oa.examine.entity.ExamineLeave;
import com.sancai.oa.user.entity.User;
import lombok.Data;

import java.util.List;

/**
 * 用户与请假公休集合对象
 */
@Data
public class UserExamineLeaveHolidayDTO {
    List<User> userList;
    List<ExamineLeave> examineLeaveList;
    List<ExamineHoliday> examineHolidayList;
    long intervalTimeEnd;
}
