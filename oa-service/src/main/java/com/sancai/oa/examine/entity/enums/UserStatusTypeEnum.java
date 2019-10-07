package com.sancai.oa.examine.entity.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 员工类型枚举
 * @author pisaquan
 * @since 2019/8/21 13:40
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum UserStatusTypeEnum implements IEnum<String> {

    /**
     *  员工类型枚举
     */
    INSERVICE("INSERVICE", "在职"),
    DIMISSION("DIMISSION", "离职");

    private String key;
    private String value;

    UserStatusTypeEnum(final String key, final String value) {
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
        UserStatusTypeEnum[] leaveTypeEnums = values();
        for (UserStatusTypeEnum leaveTypeEnum : leaveTypeEnums) {
            if (leaveTypeEnum.getValue().equals(value)) {
                return leaveTypeEnum.getKey();
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
        UserStatusTypeEnum[] leaveTypeEnums = values();
        for (UserStatusTypeEnum leaveTypeEnum : leaveTypeEnums) {
            if (leaveTypeEnum.getKey().equals(key)) {
                return leaveTypeEnum.getValue();
            }
        }
        return null;
    }



}
