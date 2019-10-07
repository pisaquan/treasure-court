package com.sancai.oa.typestatus.exception;

import com.sancai.oa.core.exception.OaError;

public enum EnumTypeStatusError implements OaError {
    QUERY_ENUM_FAILURE(20121001,"查询枚举类型失败"),
    QUERY_IS_EMPTY(20121002,"查询枚举类型结果为空")
    ;
    private Integer code;
    private String message;
    EnumTypeStatusError(Integer code,String message){
        this.code=code;
        this.message=message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Integer getCode() {
        return code;
    }
}
