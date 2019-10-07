package com.sancai.oa.log.config;

/**
 * @author fanjing
 * @create 2019/7/25
 * @description 操作模块
 */
public enum LogModelEnum implements EnumSuper {
    EXAMINE_POSITION("EXAMINE_POSITION","岗位考核"),
    EXAMINE_ACTION("EXAMINE_ACTION","行为考核"),
    EXAMINE_HOLIDAY("EXAMINE_HOLIDAY","公休"),
    CLOCKIN("CLOCKIN","打卡"),
    SCORE("SCORE","积分"),
    EXAMINE_TEMPLATE("EXAMINE_TEMPLATE","审批模板"),
    SIGNIN_CONFIRM("SIGNIN_CONFIRM","签到确认"),
    COMPANY("COMPANY","分公司"),
    REPORT("REPORT","日志"),
    REPORT_RULE("REPORT_RULE","日志规则"),
    ATTENDANCE("ATTENDANCE","考勤"),
    LEAVE("LEAVE","请假");


    private String key;
    private String value;

    LogModelEnum(String key,String value){
        this.key=key;
        this.value=value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }
    /**
     *
     * @param key 数据库中存储的字段(英文字符串)
     * @return 返回枚举类型中key对应的value(中文字符串)
     */
    public static String getvalueBykey(String key){
        for (LogModelEnum logModelEnum : values()) {
            if(logModelEnum.getKey().equals(key)){
                return logModelEnum.getValue();
            }
        }
        return null;
    }


}
