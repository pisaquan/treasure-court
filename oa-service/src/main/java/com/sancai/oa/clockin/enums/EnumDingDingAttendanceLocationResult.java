package com.sancai.oa.clockin.enums;

/**
 * 钉钉考勤位置结果
 * @Author chenm
 * @create 2019/7/26 11:48
 */

public enum EnumDingDingAttendanceLocationResult {
    /**
     *范围内
     */
    Normal("Normal", "范围内"),
    /**
     *范围外
     */
    Outside("Outside","范围外"),
    /**
     *未打卡
     */
    NotSigned("NotSigned","未打卡");


    private String key;

    private String value;

    EnumDingDingAttendanceLocationResult(String key, String value) {
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
