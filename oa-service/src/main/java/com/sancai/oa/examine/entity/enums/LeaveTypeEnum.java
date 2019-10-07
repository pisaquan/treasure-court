package com.sancai.oa.examine.entity.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 请假类型枚举
 * @author pisaquan
 * @since 2019/8/2 13:40
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum LeaveTypeEnum implements IEnum<String> {

    /**
     *  请假类型枚举
     */
    CASUALLEAVE("CASUALLEAVE", "事假"),
    SICKLEAVE("SICKLEAVE", "病假"),
    MATERNITYLEAVE("MATERNITYLEAVE", "产假"),
    PATAERNITYLEAVE("PATAERNITYLEAVE", "陪产假"),
    MARRIAGELEAVE("MARRIAGELEAVE", "婚假"),
    FUNERALLEAVE("FUNERALLEAVE", "丧假"),
    PUBLICHOLIDAY("PUBLICHOLIDAY", "公休假"),
    LEGALHOLIDAY("LEGALHOLIDAY", "法定假"),
    PAID("PAID", "带薪"),
    UNPAID("UNPAID", "无薪");

    private String key;
    private String value;

    LeaveTypeEnum(final String key, final String value) {
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
        LeaveTypeEnum[] leaveTypeEnums = values();
        for (LeaveTypeEnum leaveTypeEnum : leaveTypeEnums) {
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
        LeaveTypeEnum[] leaveTypeEnums = values();
        for (LeaveTypeEnum leaveTypeEnum : leaveTypeEnums) {
            if (leaveTypeEnum.getKey().equals(key)) {
                return leaveTypeEnum.getValue();
            }
        }
        return null;
    }



}
