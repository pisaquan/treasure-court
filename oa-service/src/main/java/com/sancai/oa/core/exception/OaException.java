package com.sancai.oa.core.exception;

import lombok.Data;

/**
 * 自定义异常
 * @Author chenm
 * @create 2019/7/22 14:45
 */
@Data
public class OaException extends RuntimeException{
    private OaError error;
    public OaException(OaError error){
        this.error=error;
    }
}