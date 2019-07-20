package com.sancai.oasystem.bean.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 审批类型枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExamineTypeEnum implements IEnum<String> {

    /**
     *   审批类型枚举
     */
    LEAVE("leave", "请假"),
    HOLIDAY("holiday", "公休假"),
    BUSINESSTRAVEL("businessTravel", "出差");

    private String value;
    private String desc;

    ExamineTypeEnum(final String value, final String desc) {
        this.value = value;
        this.desc = desc;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public String getDesc(){
        return this.desc;
    }
}
