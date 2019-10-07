package com.sancai.oa.log.config;

/**
 * @author fanjing
 * @create 2019/7/25
 * @description 操作类型
 */
public enum LogOperationTypeEnum implements EnumSuper {
    SAVE("SAVE","保存"),
    UPDATE("UPDATE","修改"),
    DELETE("DELETE","删除");

    private String key;
    private String value;

    LogOperationTypeEnum(String value, String description){
        this.key=value;
        this.value=description;
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
        for (LogOperationTypeEnum logOperationTypeEnum : values()) {
            if(logOperationTypeEnum.getKey().equals(key)){
                return logOperationTypeEnum.getValue();
            }
        }
        return null;
    }
}
