package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/1
 * @description 签到记录状态枚举类
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SigninRecordStatusEnum {

    NEW("NEW", "未判断"),
    VALID("VALID", "有效"),
    INVALID("INVALID", "无效");
    private String key;
    private String value;

    SigninRecordStatusEnum(String key, String value) {
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
        for (SigninRecordStatusEnum signinRecordStatusEnum : values()) {
            if(signinRecordStatusEnum.getKey().equals(key)){
                return signinRecordStatusEnum.getValue();
            }
        }
        return null;
    }


}