package com.sancai.oa.signinconfirm.exception;


import com.sancai.oa.core.exception.OaError;
import com.sancai.oa.core.exception.OaException;

/**
 * @author wangyl
 * @date 2019-07-22 09:39
 */
public class OaSigninConfirmlException extends OaException {

    public OaSigninConfirmlException(OaError error){
        super(error);
 
    }

    
}
