package com.sancai.oa.score.entity.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sancai.oa.examine.entity.enums.LeaveTypeEnum;

/**
 * 积分枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ScoreTypeEnum implements IEnum<String> {

    /**
     *   积分类型枚举
     */
    SCOREADD("ADD", "奖金"),
    SCORESUBTRACT("SUBTRACT", "惩罚");

    private String key;
    private String value;

    ScoreTypeEnum(final String key, final String value) {
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
     * 根据value获取枚举key
     * @param value
     * @return
     */
    public static String getKeyByValue(String value) {
        ScoreTypeEnum[] scoreTypeEnums = values();
        for (ScoreTypeEnum scoreTypeEnum : scoreTypeEnums) {
            if (scoreTypeEnum.getValue().equals(value)) {
                return scoreTypeEnum.getKey();
            }
        }
        return null;
    }

    /**
     * 根据key获取枚举value
     * @param key
     * @return
     */
    public static String getValueBykey(String key) {
        ScoreTypeEnum[] scoreTypeEnums = values();
        for (ScoreTypeEnum scoreTypeEnum : scoreTypeEnums) {
            if (scoreTypeEnum.getKey().equals(key)) {
                return scoreTypeEnum.getValue();
            }
        }
        return null;
    }
}
