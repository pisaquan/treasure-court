package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 考勤状态枚举类，汉字--->符号
 * @author fanjing
 * @date 2019/8/17
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AttendanceStatusCharacterEnum {
    /**
     * 正常打卡
     */
    NORMAL("出勤", "D"),
    /**
     * 签到改为正常
     */
    SIGNIN("签到","D1"),
    /**
     * 人工订正改为正常
     */
    MANUALCORRECTION("人工订正","D2"),
    EARLY("早退","E"),
    LATE("迟到","L"),
    NOTSIGNED("漏打卡","C"),
    ABSENTEEISM("旷工","✖"),
    BUSINESSTRAVEL("出差","M"),
    HOLIDAY("公休","公"),
    CASUALLEAVE("事假", "△"),
    UNPAIDSICKLEAVE("无薪病假", "○"),
    PAIDSICKLEAVE("带薪病假", "○1"),
    MATERNITYLEAVE("产假", "产"),
    MARRIAGELEAVE("婚假", "婚"),
    FUNERALLEAVE("丧假", "丧"),
    PATAERNITYLEAVE("陪产假","陪");


    private String key;
    private String value;

    AttendanceStatusCharacterEnum(String key, String value) {
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
        for (AttendanceStatusCharacterEnum attendanceStatusEnums : values()) {
            if(attendanceStatusEnums.getKey().equals(key)){
                return attendanceStatusEnums.getValue();
            }
        }
        return null;
    }
}
