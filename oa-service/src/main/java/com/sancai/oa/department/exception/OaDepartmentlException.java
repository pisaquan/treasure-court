package com.sancai.oa.department.exception;


import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;

/**
 * @Author wangyl
 * @create 2019/7/25 09:14
 */
public class OaDepartmentlException extends OaException {

    public OaDepartmentlException(OaError error){
        super(error);
 
    }

}
