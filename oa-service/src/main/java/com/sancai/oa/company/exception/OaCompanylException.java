package com.sancai.oa.company.exception;

import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;

/**
 * @Author wangyl
 * @create 2019/7/25 09:14
 */
public class OaCompanylException extends OaException {

    public OaCompanylException(OaError error){
        super(error);
 
    }

}
