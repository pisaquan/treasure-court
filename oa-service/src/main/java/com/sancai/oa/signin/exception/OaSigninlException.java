package com.sancai.oa.signin.exception;


import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;

/**
 * @author chenm
 */
public class OaSigninlException extends OaException {

    public OaSigninlException(OaError error){
        super(error);
 
    }

    
}
