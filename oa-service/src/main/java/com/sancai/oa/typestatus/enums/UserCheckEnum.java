package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author wangyl
 * @date 2019/8/2
 * @description 请假统计中的请假类型
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserCheckEnum {

    EQUALS(1, "精确匹配"),
    LIKE(2, "模糊匹配"),
    ;
    private int key;
    private String value;

    UserCheckEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    public int getKey() {
        return key;
    }
    /**
     *
     * @param key 数据库中存储的字段(英文字符串)
     * @return 返回枚举类型中key对应的value(中文字符串)
     */
    public static String getvalueBykey(int key){
        for (UserCheckEnum leaveCountTypeEnum : values()) {
            if(leaveCountTypeEnum.getKey() ==key ){
                return leaveCountTypeEnum.getValue();
            }
        }
        return null;
    }

}
