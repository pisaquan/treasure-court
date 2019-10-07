package com.sancai.oa.report.entity.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 日志记录类型枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ReportRecordTypeEnum implements IEnum<String> {

    /**
     *   日志记录状态类型枚举
     */
    NEW("NEW","未判断"),
    ABNORMAL("ABNORMAL","异常"),
    NORMAL("NORMAL","正常"),
    WARN("WARN","警告"),

    /**
     * 日志记录提交时间异常结果类型枚举
     */
    BEFORE ("BEFORE ","早交"),
    AFTER("AFTER","迟交");

    private String key;
    private String value;

    ReportRecordTypeEnum(final String key, final String value) {
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
        for (ReportRecordTypeEnum reportRecordTypeEnum : ReportRecordTypeEnum.values()) {
            if (key.equals(reportRecordTypeEnum.key)) {
                return reportRecordTypeEnum.getValue();
            }
        }
        return key;
    }

}
