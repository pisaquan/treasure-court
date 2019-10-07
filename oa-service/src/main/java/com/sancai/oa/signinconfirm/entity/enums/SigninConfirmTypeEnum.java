package com.sancai.oa.signinconfirm.entity.enums;

import com.baomidou.mybatisplus.core.enums.IEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 签到类型枚举
 * @author wangyl
 * @since 2019-08-02
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum SigninConfirmTypeEnum implements IEnum<String> {

    /**
     *   签到数据状态类型枚举
     */
    NEW("NEW","未判断"),
    INVALID("INVALID","无效"),
    VALID("VALID","有效");

    private String value;
    private String msg;

    SigninConfirmTypeEnum(final String value, final String msg) {
        this.value = value;
        this.msg = msg;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    public String getMsg(){
        return this.msg;
    }

    /**
     *根据value取msg
     * @param value 枚举的Value
     * @return String 枚举的msg
     */
    public static String getMsgByValue(String value) {
        for (SigninConfirmTypeEnum signinTypeEnum : SigninConfirmTypeEnum.values()) {
            if (value.equals(signinTypeEnum.value)) {
                return signinTypeEnum.getMsg();
            }
        }
        return value;
    }

}
