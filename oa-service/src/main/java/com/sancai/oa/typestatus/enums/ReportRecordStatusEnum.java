package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/10
 * @description 日志记录状态
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ReportRecordStatusEnum {
    NEW("NEW","未判断"),
    NORMAL("NORMAL","正常"),
    ABNORMAL("ABNORMAL","异常"),
    WARN("WARN","警告"),
    ;
    private String key;
    private String value;

    ReportRecordStatusEnum(String key, String value) {
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
        for (ReportRecordStatusEnum examineTemplateTypeEnum : values()) {
            if(examineTemplateTypeEnum.getKey().equals(key)){
                return examineTemplateTypeEnum.getValue();
            }
        }
        return null;
    }
}
