package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/2
 * @description 请假统计中的请假类型
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum LeaveCountTypeEnum {

    CASUALLEAVE("CASUALLEAVE", "事假"),
    SICKLEAVE("SICKLEAVE", "病假"),
    MATERNITYLEAVE("MATERNITYLEAVE", "产假"),
    MARRIAGELEAVE("MARRIAGELEAVE", "婚假"),
    FUNERALLEAVE("FUNERALLEAVE", "丧假"),
    PATAERNITYLEAVE("PATAERNITYLEAVE","陪产假")
    ;
    private String key;
    private String value;

    LeaveCountTypeEnum(String key, String value) {
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
        for (LeaveCountTypeEnum leaveCountTypeEnum : values()) {
            if(leaveCountTypeEnum.getKey().equals(key)){
                return leaveCountTypeEnum.getValue();
            }
        }
        return null;
    }

}
