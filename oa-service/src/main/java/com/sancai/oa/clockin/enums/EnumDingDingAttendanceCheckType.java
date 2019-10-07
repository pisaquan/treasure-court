package com.sancai.oa.clockin.enums;

/**
 * 钉钉考勤类型
 * @Author chenm
 * @create 2019/7/26 11:48
 */

public enum EnumDingDingAttendanceCheckType {
    /**
     *范围内
     */
    OnDuty("OnDuty", "上班"),
    /**
     *未打卡
     */
    OffDuty("OffDuty","下班");


    private String key;

    private String value;

    EnumDingDingAttendanceCheckType(String key, String value) {
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
