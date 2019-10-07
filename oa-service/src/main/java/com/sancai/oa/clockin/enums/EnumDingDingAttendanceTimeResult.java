package com.sancai.oa.clockin.enums;

/**
 * 钉钉考勤时间结果
 * @Author chenm
 * @create 2019/7/26 11:48
 */

public enum EnumDingDingAttendanceTimeResult {
    /**
     *正常
     */
    Normal("Normal", "正常"),
    /**
     *早退
     */
    Early("Early","早退"),
    /**
     *迟到
     */
    Late("Late","迟到"),
    /**
     *严重迟到
     */
    SeriousLate("SeriousLate","严重迟到"),
    /**
     *旷工迟到
     */
    Absenteeism("Absenteeism","旷工迟到"),
    /**
     *未打卡
     */
    NotSigned("NotSigned","未打卡");


    private String key;

    private String value;

    EnumDingDingAttendanceTimeResult(String key, String value) {
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
