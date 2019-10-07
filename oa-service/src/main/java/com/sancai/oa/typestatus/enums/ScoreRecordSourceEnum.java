package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/2
 * @description 积分记录变动来源
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ScoreRecordSourceEnum {
    EXAMINEPOSITION("EXAMINEPOSITION", "岗位考核"),
    EXAMINEACTION("EXAMINEACTION", "行为考核"),
    /**
     * 人工订正（对一名员工进行积分变动）
     */
    MANUALCORRECTION("MANUALCORRECTION","人工订正"),

    NOTSIGNEDTWO("NOTSIGNEDTWO", "缺卡2次"),
    NOTSIGNEDTHREE("NOTSIGNEDTHREE", "缺卡3次"),
    LATETHIRTYMINUTES("LATETHIRTYMINUTES", "迟到30分钟及以内"),
    LATESIXTYMINUTES("LATESIXTYMINUTES", "迟到60分钟及以内"),
    LEAVEEARLYTHIRTYMINUTES("LEAVEEARLYTHIRTYMINUTES", "早退30分钟及以内"),
    LEAVEEARLYSIXTYMINUTES("LEAVEEARLYSIXTYMINUTES", "早退60分钟及以内"),
    REPORTISNULL("REPORTISNULL", "日报未提交"),
    REPORTFORMATISINVALID("REPORTFORMATISINVALID", "日报格式填写不规范"),
    AMENDSUBMISSIONTIMEINCONSISTENCY("AMENDSUBMISSIONTIMEINCONSISTENCY","修正特殊情况提交时间无效的日报"),
    NOTSIGNEDFIRSTWARNING("NOTSIGNEDFIRSTWARNING", "缺卡第一次扣积分警告"),
    LATEEARLYFIRSTWARNING("LATEEARLYFIRSTWARNING", "迟到早退第一次扣积分警告"),
    REPORTLATEDELIVERYTHIRTYMINUTES("REPORTLATEDELIVERYTHIRTYMINUTES", "日报晚交30分钟及以内"),
    REPORLATEDELIVERYESIXTYMINUTES("REPORLATEDELIVERYESIXTYMINUTES", "日报晚交60分钟以内"),
    REPORLATEDELIVERYESIXTYMINUTESMORE("LATEDELIVERYESIXTYMINUTES", "日报晚交60分钟及以上"),
    REPORTEARLYDELIVERYTHIRTYMINUTES("REPORTEARLYDELIVERYTHIRTYMINUTES", "日报早交30分钟及以内"),
    REPORTEARLYDELIVERYSIXTYMINUTES("REPORTEARLYDELIVERYSIXTYMINUTES", "日报早交60分钟及以内"),
    REPORTELEAVEEARLYFIRSTWARNING("REPORTELEAVEEARLYFIRSTWARNING", "日报早交第一次扣积分警告"),
    REPORTELATEFIRSTWARNING("REPORTELATEFIRSTWARNING", "日报晚交第一次扣积分警告")
    ;
    private String key;
    private String value;

    ScoreRecordSourceEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    public String getKey(){
        return key;
    }
    /**
     *
     * @param key 数据库中存储的字段(英文字符串)
     * @return 返回枚举类型中key对应的value(中文字符串)
     */
    public static String getvalueBykey(String key){
        for (ScoreRecordSourceEnum scoreRecordSourceEnum : values()) {
            if(scoreRecordSourceEnum.getKey().equals(key)){
                return scoreRecordSourceEnum.getValue();
            }
        }
        return null;
    }


}
