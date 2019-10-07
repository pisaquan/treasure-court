package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 考勤状态枚举类，汉字--->符号
 * @author fanjing
 * @date 2019/8/17
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AttendanceConfirmCharacterEnum {
    /**
     * 未发送
     */
    NOT_SEND("0", "未发送"),
    /**
     * 发送未确认
     */
    SEND_NOT_CONFIRM("1","已发送未确认"),
    /**
     * 用户已确认
     */
    USER_CONFIRMD("2","用户已确认");



    private String key;
    private String value;

    AttendanceConfirmCharacterEnum(String key, String value) {
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
        for (AttendanceConfirmCharacterEnum attendanceStatusEnums : values()) {
            if(attendanceStatusEnums.getKey().equals(key)){
                return attendanceStatusEnums.getValue();
            }
        }
        return null;
    }
}
