package com.sancai.oa.signin.entity.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 签到类型枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SigninTypeEnum implements IEnum<String> {

    /**
     *   签到数据状态类型枚举
     */
    NEW("NEW","未判断"),
    INVALID("INVALID","无效"),
    VALID("VALID","有效"),
    COMPLETED("COMPLETED","已完成"),
    UNCOMPLETED("UNCOMPLETED","未完成")
    ;
    private String key;
    private String value;

    SigninTypeEnum(final String key, final String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public String getKey(){
        return this.key;
    }

    /**
     *根据key取Value
     * @param key 枚举的key
     * @return String 枚举的value
     */
    public static String getMsgByValue(String key) {
        for (SigninTypeEnum signinTypeEnum : SigninTypeEnum.values()) {
            if (key.equals(signinTypeEnum.key)) {
                return signinTypeEnum.getValue();
            }
        }
        return key;
    }

}
