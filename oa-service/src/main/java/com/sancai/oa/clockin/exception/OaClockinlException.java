package com.sancai.oa.clockin.exception;

import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;

/**
 * @Author fans
 * @create 2019/7/25 09:14
 */
public class OaClockinlException extends OaException {

    public OaClockinlException(OaError error){
        super(error);
 
    }

}
