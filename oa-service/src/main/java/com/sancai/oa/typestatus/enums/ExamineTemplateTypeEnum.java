package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/2
 * @description 审批模板类型
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExamineTemplateTypeEnum {
    EXAMINEACTIONFORM("EXAMINEACTIONFORM","行为考核奖惩单"),
    EXAMINEPOSITIONFORM("EXAMINEPOSITIONFORM","岗位考核奖惩单"),
    BUSINESSTRAVEL("BUSINESSTRAVEL","出差申请"),
    LEAVEAPPLY("LEAVEAPPLY","请假申请"),
    HOLIDAYAPPLY("HOLIDAYAPPLY","休假申请"),
    OUTAPPLY("OUTAPPLY","外出申请"),
    ;
    private String key;
    private String value;

    ExamineTemplateTypeEnum(String key, String value) {
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
        for (ExamineTemplateTypeEnum examineTemplateTypeEnum : values()) {
            if(examineTemplateTypeEnum.getKey().equals(key)){
                return examineTemplateTypeEnum.getValue();
            }
        }
        return null;
    }
}
