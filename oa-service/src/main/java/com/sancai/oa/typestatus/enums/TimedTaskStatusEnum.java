package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/2
 * @description 定时任务状态枚举类
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TimedTaskStatusEnum {
    SUCCESS("SUCCESS", "成功"),
    FAILURE("FAILURE", "失败"),
    EXECUTING("EXECUTING", "执行中"),
    RETRYING("RETRYING", "重试中");


    private String key;
    private String value;

    TimedTaskStatusEnum(String key, String value) {
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
        for (TimedTaskStatusEnum timedTaskStatusEnum : values()) {
            if(timedTaskStatusEnum.getKey().equals(key)){
                return timedTaskStatusEnum.getValue();
            }
        }
        return null;
    }
}
