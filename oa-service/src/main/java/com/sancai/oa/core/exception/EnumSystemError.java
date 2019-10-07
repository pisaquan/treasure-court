package com.sancai.oa.core.exception;

/**
 * 系统异常枚举
 * @Author chenm
 * @create 2019/7/22 14:43
 */
public enum EnumSystemError implements OaError {
    /**
     * Error. 头4位：1000
     */
    SYSTEM_ERROR(10001001, "系统错误"),
    DINGDING_ERROR(10001002, "钉钉接口异常"),
    PROD_NO_GRAB(10001003, "线上环境不允许抓取工具执行"),
    NO_ACCESS_PERMISSION(10001004, "没有访问权限"),
    ;

    private Integer code;

    private String message;

    EnumSystemError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }


    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }



}