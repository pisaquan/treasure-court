package com.sancai.oa.log.exception;

import com.sancai.oa.core.exception.OaError;

/**
 * @author fanjing
 * @date  2019/7/25
 */
public enum EnumLogError implements OaError {
    LOGGING_FAILURE (20100001,"记录日志失败");

    private Integer code;

    private String message;

    EnumLogError(Integer code, String message) {
        this.code = code;
        this.message = message;
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
