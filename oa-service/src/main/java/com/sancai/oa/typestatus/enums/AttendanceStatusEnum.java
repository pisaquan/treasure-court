package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 考勤状态枚举类  英文---->符号
 * @author fanjing
 * @date 2019/8/12
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AttendanceStatusEnum {
    /**
     * 正常打卡
     */
    NORMAL("NORMAL", "D"),
    /**
     * 签到改为正常
     */
    SIGNIN("SIGNIN","D1"),
    /**
     * 人工订正改为正常
     */
    MANUALCORRECTION("MANUALCORRECTION","D2"),
    EARLY("EARLY","E"),
    LATE("LATE","L"),
    NOTSIGNED("NOTSIGNED","C"),
    ABSENTEEISM("ABSENTEEISM","✖"),
    BUSINESSTRAVEL("BUSINESSTRAVEL","M"),
    HOLIDAY("HOLIDAY","公"),
    CASUALLEAVE("CASUALLEAVE", "△"),
    SICKLEAVE("SICKLEAVE", "○"),
    MATERNITYLEAVE("MATERNITYLEAVE", "产"),
    MARRIAGELEAVE("MARRIAGELEAVE", "婚"),
    FUNERALLEAVE("FUNERALLEAVE", "丧"),
    UNPAIDSICKLEAVE("UNPAIDSICKLEAVE","○"),
    PAIDSICKLEAVE("PAIDSICKLEAVE", "○1"),
    PATAERNITYLEAVE("PATAERNITYLEAVE","陪");


    private String key;
    private String value;

    AttendanceStatusEnum(String key, String value) {
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
        for (AttendanceStatusEnum attendanceStatusEnums : values()) {
            if(attendanceStatusEnums.getKey().equals(key)){
                return attendanceStatusEnums.getValue();
            }
        }
        return null;
    }
}
