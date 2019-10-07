package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/2
 * @description 公休统计中的请假类型
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum HolidayCountTypeEnum  {
    PUBLICHOLIDAY("PUBLICHOLIDAY", "公休假"),
    LEGALHOLIDAY("LEGALHOLIDAY", "法定假")
    ;
    private String key;
    private String value;

    HolidayCountTypeEnum(String key, String value) {
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
        for (HolidayCountTypeEnum holidayCountTypeEnum : values()) {
            if(holidayCountTypeEnum.getKey().equals(key)){
                return holidayCountTypeEnum.getValue();
            }
        }
        return null;
    }



}
