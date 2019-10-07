package com.sancai.oa.typestatus.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author fanjing
 * @date 2019/8/2
 * @decription 定时任务类型
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum TimedTaskTypeEnum {
    /**
     * 抓取任务
     */
    GRAB_TASK("GRAB_TASK","抓取任务"),
    /**
     * 更新任务
     */
    UPDATE_TASK("UPDATE_TASK","更新任务"),
    /**
     * 通知任务
     */
    NOTIFY_TASK("NOTIFY_TASK","通知任务"),
    /**
     * 合并任务
     */
    MERGE_TASK("MERGE_TASK","合并任务")
    ;
    private String key;
    private String value;

    TimedTaskTypeEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    public String getkey() {
        return key;
    }


    /**
     *
     * @param key 数据库中存储的字段(英文字符串)
     * @return 返回枚举类型中key对应的value(中文字符串)
     */
    public static String getvalueBykey(String key){
        for (TimedTaskTypeEnum timedTaskTypeEnum : values()) {
            if(timedTaskTypeEnum.getkey().equals(key)){
                return timedTaskTypeEnum.getValue();
            }
        }
        return null;
    }
}
