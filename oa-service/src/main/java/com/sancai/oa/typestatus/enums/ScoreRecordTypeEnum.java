package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/2
 * @description 积分记录变动类型
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ScoreRecordTypeEnum {
    SCOREADD("ADD", "奖金"),
    SCORESUBTRACT("SUBTRACT", "惩罚")
    ;
    private String key;
    private String value;

    ScoreRecordTypeEnum(String key, String value) {
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
        for (ScoreRecordTypeEnum scoreRecordTypeEnum : values()) {
            if(scoreRecordTypeEnum.getKey().equals(key)){
                return scoreRecordTypeEnum.getValue();
            }
        }
        return null;
    }


}
