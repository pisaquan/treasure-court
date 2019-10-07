package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/2
 * @description 外出签到确认状态
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SigninConfirmStatusEnum {
    COMPLETED("COMPLETED","已完成"),
    UNCOMPLETED("UNCOMPLETED","未完成")
    ;
    private String key;
    private String value;

    SigninConfirmStatusEnum(String key, String value) {
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
        for (SigninConfirmStatusEnum signinConfirmStatusEnum : values()) {
            if(signinConfirmStatusEnum.getKey().equals(key)){
                return signinConfirmStatusEnum.getValue();
            }
        }
        return null;
    }



}
