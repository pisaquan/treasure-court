package com.sancai.oa.dingding.role;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 钉钉考勤专员类型枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum HrTypeEnum implements IEnum<String> {

    /**
     *   钉钉考勤专员枚举
     */
    HR_GROUP("hr_group","行政人事"),
    HR_NAME("hr_name","考勤专员");


    private String key;
    private String value;

    HrTypeEnum(final String key, final String value) {
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
        for (HrTypeEnum msgTypeEnum : HrTypeEnum.values()) {
            if (key.equals(msgTypeEnum.key)) {
                return msgTypeEnum.getValue();
            }
        }
        return null;
    }

}
