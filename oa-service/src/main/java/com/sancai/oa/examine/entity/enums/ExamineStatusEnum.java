package com.sancai.oa.examine.entity.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 审批状态枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExamineStatusEnum implements IEnum<String> {

    /**
     *   审批状态枚举
     */
    NEW("NEW", "新创建"),
    RUNNING("RUNNING", "运行中"),
    TERMINATED("TERMINATED", "被终止"),
    COMPLETED("COMPLETED", "完成"),
    AGREE("agree", "同意"),
    REFUSE("refuse", "拒绝");

    private String key;
    private String value;

    ExamineStatusEnum(final String key, final String value) {
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
