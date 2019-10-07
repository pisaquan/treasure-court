package com.sancai.oa.report.entity.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 日志模板状态类型枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ReportTemplateTypeEnum implements IEnum<String> {

    /**
     *   日志模板状态状态类型枚举
     */
    INVALID("INVALID","无效"),
    VALID("VALID","有效");


    private String key;
    private String value;

    ReportTemplateTypeEnum(final String key, final String value) {
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
        for (ReportTemplateTypeEnum reportTemplateTypeEnum : ReportTemplateTypeEnum.values()) {
            if (key.equals(reportTemplateTypeEnum.key)) {
                return reportTemplateTypeEnum.getValue();
            }
        }
        return key;
    }

}
