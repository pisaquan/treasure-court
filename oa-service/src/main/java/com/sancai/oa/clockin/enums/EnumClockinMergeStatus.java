package com.sancai.oa.clockin.enums;

/**
 * 考勤打卡点枚举
 * @Author chenm
 * @create 2019/7/26 11:48
 */

public enum EnumClockinMergeStatus {
    /**
     * 未合并完成
     */
    NEW("NEW", "未合并完成"),
    /**
     * 已合并完成
     */
    DONE("DONE","已合并完成"),

    STATUS("status", "打卡点状态"),
    ID("id", "打卡点id"),
    BASECHECKTIME("baseCheckTime", "打卡点基准时间"),
    USERCHECKTIME("userCheckTime", "实际打卡时间"),
    WORKINGFOUR("4", "考勤班次为4次"),
    WORKINGTWO("2", "考勤班次为2次");



    private String key;

    private String value;

    EnumClockinMergeStatus(String key, String value) {
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
