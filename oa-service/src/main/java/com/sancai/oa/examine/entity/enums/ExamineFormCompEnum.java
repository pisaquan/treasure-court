package com.sancai.oa.examine.entity.enums;

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
    DDGOOUTFILED("DDGooutField","外出套件"),
    DDGOOUTLOCATION("DDGooutLocation","外出地点"),
    DDHOLIDAYFIELDLENGTH("6","请假休假外出组件返回值长度"),
    DDDATERANGEFIELD("DDDateRangeField","日期区间组件"),
    TABLEFIELD("TableField","明细组件"),
    BUSINESSTRIPDETAIL("businessTripDetail","出差明细"),
    POSTASSESSMENT("postAssessment","岗位考核"),
    BEHAVIORASSESSMENT("behaviorAssessment","行为考核"),
    DDDATERANGELENGTH("3","日期区间组件返回值长度"),
    TEXTAREAFIELD("TextareaField","多行输入框"),
    DDPHOTOFIELD("DDPhotoField","图片"),
    COMPANY("company", "所属公司"),
    SALARY("salary", "请假性质"),
    REASON("reason", "请假原因"),
    PHOTO("photo", "图片"),
    DEPARTMENT("department", "所在部门"),
    MORNING("morning", "上午"),
    AFTERNOON("afternoon", "下午"),
    HOLIDAYTYPE("holiday", "请假类型"),
    TIMERANGE("timeRange","[\"开始时间\",\"结束时间\"]"),
    LABEL("label", "标题"),
    TRAVELREASON("travelReason", "出差事由"),
    OUTAPPLYREASON("outApplyReason", "经办事由"),
    FROMCITY("fromCity", "出发城市"),
    TOCITY("toCity", "目的城市"),
    COMPONENTTYPE("componentType", "组件类型"),
    COMONENTVALUE("value", "组件值"),
    STARTTRANSPORT("startTransport", "出发交通工具"),
    FINISHTRANSPORT("finishTransport", "返回交通工具"),
    HOTELTYPE("hotelType", "住宿安排"),
    ROWVALUE("rowValue", "出差返回json数据"),
    REMARK("remark", "备注"),
    //NAME("name", "姓名"),
    STAFF("staff", "员工"),
    DIMISSIONSTAFF("dimissionStaff", "离职员工"),
    EXTENDVALUE("extendValue", "扩展值"),
    REWARDSPUNISHTYPE("rewardsPunishType", "类别"),
    REWARDSPUNISHRULE("rewardsPunishRule", "依据岗位考核第几条依据"),
    BEHAVIORPUNISHRULE("behaviorPunishRule", "依据行为考核第几条依据"),
    SCORE("score", "分数"),
    REWARDSPUNISHREASON("rewardsPunishReason", "事由"),
    EMPLID("emplId", "扩展值userId"),
    ISNOTPUNCH("isNotPunch", "是否可以正常打卡"),
    NONORMALCLOCK("noNormalClock", "未能正常打卡时间"),
    ISINSERVICE("isInservice", "是否在职");


    private String key;
    private String value;

    ExamineFormCompEnum(final String key, final String value) {
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
