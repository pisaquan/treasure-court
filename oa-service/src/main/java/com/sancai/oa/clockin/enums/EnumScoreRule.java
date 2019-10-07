package com.sancai.oa.clockin.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sancai.oa.examine.entity.enums.LeaveTypeEnum;

/**
 * 积分规则枚举
 * @Author quanleilei
 * @create 2019/8/10 16:21
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum EnumScoreRule implements IEnum<String> {

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
    REPORTELATEFIRSTWARNING("REPORTELATEFIRSTWARNING", "日报晚交第一次扣积分警告");




    private String key;
    private String value;

    EnumScoreRule(final String key, final String value) {
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
     * 根据value获取枚举key
     * @param value
     * @return
     */
    public static String getKeyByValue(String value) {
        EnumScoreRule[] scoreRuleEnums = values();
        for (EnumScoreRule scoreRuleEnum : scoreRuleEnums) {
            if (scoreRuleEnum.getValue().equals(value)) {
                return scoreRuleEnum.getKey();
            }
        }
        return null;
    }

    /**
     * 根据key获取枚举value
     * @param key
     * @return
     */
    public static String getValueBykey(String key) {
        EnumScoreRule[] scoreRuleEnums = values();
        for (EnumScoreRule scoreRuleEnum : scoreRuleEnums) {
            if (scoreRuleEnum.getKey().equals(key)) {
                return scoreRuleEnum.getValue();
            }
        }
        return null;
    }
}
