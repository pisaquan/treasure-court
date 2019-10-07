package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/2
 * @decription 定时任务key
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TimedTaskKeyEnum {
    /**
     * 定时任务
     */
    SIGNIN_GRAB("SIGNIN_GRAB","签到抓取"),
    LEAVE_GRAB("LEAVE_GRAB","请假抓取"),
    LEAVE_UPDATE("LEAVE_UPDATE","请假更新"),
    HOLIDAY_GRAB("HOLIDAY_GRAB","休假抓取"),
    HOLIDAY_UPDATE("HOLIDAY_UPDATE","休假更新"),
    BUSINESS_TRAVEL_GRAB("BUSINESS_TRAVEL_GRAB","出差抓取"),
    BUSINESS_TRAVEL_UPDATE("BUSINESS_TRAVEL_UPDATE","出差更新"),
    EXAMINE_POSITION_GRAB("EXAMINE_POSITION_GRAB","岗位考核抓取"),
    EXAMINE_POSITION_UPDATE("EXAMINE_POSITION_UPDATE","岗位考核更新"),
    EXAMINE_ACTION_GRAB("EXAMINE_ACTION_GRAB","行为考核抓取"),
    EXAMINE_ACTION_UPDATE("EXAMINE_ACTION_UPDATE","行为考核更新"),
    OUT_APPLY_GRAB("OUT_APPLY_GRAB","外出申请抓取"),
    OUT_APPLY_UPDATE("OUT_APPLY_UPDATE","外出申请更新"),
    ATTENDANCE_GRAB("ATTENDANCE_GRAB","考勤抓取"),
    ATTENDANCE_MERGE("ATTENDANCE_MERGE","考勤合并"),
    REPORT_TEMPLATE_GRAB("REPORT_TEMPLATE_GRAB","日报模板抓取"),
    REPORT_RECORD_GRAB("REPORT_RECORD_GRAB","日报抓取"),
    DEPARTMENT_GRAB("DEPARTMENT_GRAB","部门抓取"),
    DIMISSION_EMPLOYEE_GRAB("DIMISSION_EMPLOYEE_GRAB","离职用户抓取"),
    ATTENDANCE_STATISTICS("ATTENDANCE_STATISTICS","考勤统计"),
    LEADER_SIGNIN_CONFIRM("LEADER_SIGNIN_CONFIRM","给主管发送签到确认")



    ;
    private String key;
    private String value;

    TimedTaskKeyEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    public String getKey() {
        return key;
    }


    /**
     *
     * @param key 数据库中存储的字段(英文字符串)
     * @return 返回枚举类型中key对应的value(中文字符串)
     */
    public static String getvalueBykey(String key){
        for (TimedTaskKeyEnum timedTaskKeyEnum : values()) {
            if(timedTaskKeyEnum.getKey().equals(key)){
                return timedTaskKeyEnum.getValue();
            }
        }
        return null;
    }
}
