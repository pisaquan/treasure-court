package com.sancai.oa.examine.entity.enums;

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
    LEAVE("LEAVE", "请假"),
    HOLIDAY("HOLIDAY", "休假"),
    BUSINESSTRAVEL("BUSINESSTRAVEL", "出差"),
    EXAMINEPOSITION("EXAMINEPOSITION", "岗位考核"),
    EXAMINEACTION("EXAMINEACTION", "行为考核"),
    OUTAPPLY("OUTAPPLY", "外出申请");

    private String key;
    private String value;

    ExamineTypeEnum(final String key, final String value) {
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
}
