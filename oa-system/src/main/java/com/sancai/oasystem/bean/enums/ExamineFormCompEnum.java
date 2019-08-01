package com.sancai.oasystem.bean.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 审批表单组件类型枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ExamineFormCompEnum implements IEnum<String> {

    /**
     *  审批表单组件类型
     */
    DDSELECTFIELD("DDSelectField","单选框"),
    DDHOLIDAYFIELD("DDHolidayField","请假休假组件"),
    TEXTAREAFIELD("TextareaField","多行输入框"),
    DDPHOTOFIELD("DDPhotoField","图片"),
    DEPARTMENTFIELD("DepartmentField","部门"),
    COMPANY("company", "所属公司"),
    SALARY("salary", "请假性质"),
    REASON("renson", "请假原因"),
    PHOTO("photo", "图片"),
    DEPARTMENT("department", "所在部门"),
    MORNING("morning", "上午"),
    AFTERNOON("afternoon", "下午");


    private String value;
    private String desc;

    ExamineFormCompEnum(final String value, final String desc) {
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
