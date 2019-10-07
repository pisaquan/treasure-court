package com.sancai.oa.clockin.enums;

/**
 * 每天若干次考勤点，每个考勤点的状态
 * @Author chenm
 * @create 2019/7/26 11:48
 */

public enum EnumClockinPointStatus {
    /**
     *正常打卡
     */
    NORMAL("NORMAL", "正常打卡"),
    /**
     *早退
     */
    EARLY("EARLY","早退"),
    /**
     *迟到
     */
    LATE("LATE","迟到"),
    /**
     *旷工
     */
    ABSENTEEISM("ABSENTEEISM","旷工"),
    /**
     *未打卡
     */
    NOTSIGNED("NOTSIGNED","未打卡"),
    /**
     *签到
     */
    SIGNIN("SIGNIN","签到"),

    /**
     *出差
     */
    BUSINESSTRAVEL("BUSINESSTRAVEL","出差"),

    /**
     *公休
     */
    HOLIDAY("HOLIDAY","公休"),

    /**
     *是否带薪
     */
    ISPAID("ISPAID","是否带薪");




    private String key;

    private String value;

    EnumClockinPointStatus(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
