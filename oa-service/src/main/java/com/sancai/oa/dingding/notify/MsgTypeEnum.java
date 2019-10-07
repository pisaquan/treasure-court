package com.sancai.oa.dingding.notify;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 钉钉消息类型枚举
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum MsgTypeEnum implements IEnum<String> {

    /**
     *   钉钉消息类型枚举
     */
    TEXT("text","文本消息"),
    IMAGE("image","图片"),
    FILE("file","文件"),
    OA("oa","oa消息"),
    MARKDOWN("markdown","markdown的文本"),
    ACTION_CARD("action_card","会话列表和通知的文案");


    private String key;
    private String value;

    MsgTypeEnum(final String key, final String value) {
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
     *根据key取Value
     * @param key 枚举的key
     * @return String 枚举的value
     */
    public static String getMsgByValue(String key) {
        for (MsgTypeEnum msgTypeEnum : MsgTypeEnum.values()) {
            if (key.equals(msgTypeEnum.key)) {
                return msgTypeEnum.getValue();
            }
        }
        return null;
    }

}
